package CheckerDefaultPackage;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Stack;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.javacutil.TreeUtils;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;

public class GenericEffectVisitor  extends BaseTypeVisitor<GenericEffectTypeFactory>{

	protected final boolean debugSpew;
	private GenericEffect genericEffect;
	private GenericEffectHeirarchy genericEffectHeirarchy;

    // effStack and currentMethods should always be the same size.
    protected final Stack<GenericEffect> effStack;
    protected final Stack<MethodTree> currentMethods;

    public GenericEffectVisitor(BaseTypeChecker checker) {
        super(checker);
        debugSpew = checker.getLintOption("debugSpew", false);
        if (debugSpew) {
            System.err.println("Running IOEffectVisitor");
        }
        effStack = new Stack<GenericEffect>();
        currentMethods = new Stack<MethodTree>();
    }
    
    @Override
    protected GenericEffectTypeFactory createTypeFactory() {
        return new GenericEffectTypeFactory(checker, debugSpew);
    }
    
    @Override
    protected void checkMethodInvocability(
            AnnotatedExecutableType method, MethodInvocationTree node) {
        // The inherited version of this complains about invoking methods of @Super instantiations of
        // classes, which by default are annotated @AlwaysSub, which for data type qualifiers is
        // reasonable, but it not what we want, since we want .
        // TODO: Undo this hack!
    }

    @Override
    protected boolean checkOverride(
            MethodTree overriderTree,
            AnnotatedTypeMirror.AnnotatedDeclaredType enclosingType,
            AnnotatedTypeMirror.AnnotatedExecutableType overridden,
            AnnotatedTypeMirror.AnnotatedDeclaredType overriddenType,
            Void p) {
        // Method override validity is checked manually by the type factory during visitation
        return true;
    }
    
    // Check that the invoked effect is <= permitted effect (effStack.peek())
    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        if (debugSpew) {
            System.err.println("For invocation " + node + " in " + currentMethods.peek().getName());
        }

        // Target method annotations
        ExecutableElement methodElt = TreeUtils.elementFromUse(node);//ExecutableElement: This gets the inherited methods from the base class of the method being analyzed
        if (debugSpew) {
            System.err.println("methodElt found");
        }

        MethodTree callerTree = TreeUtils.enclosingMethod(getCurrentPath());
        if (callerTree == null) {
            // Static initializer; let's assume this is safe to have the Super effect
            if (debugSpew) {
                System.err.println("No enclosing method: likely static initializer");
            }
            return super.visitMethodInvocation(node, p);
        }
        if (debugSpew) {
            System.err.println("callerTree found");
        }

        ExecutableElement callerElt = TreeUtils.elementFromDeclaration(callerTree);// What annotations has been applied
        if (debugSpew) {
            System.err.println("callerElt found");
        }

        Class<? extends Annotation> targetEffect = atypeFactory.getDeclaredEffect(methodElt);
        Class<? extends Annotation> callerEffect = atypeFactory.getDeclaredEffect(callerElt);
        // Field initializers inside anonymous inner classes show up with a null current-method ---
        // the traversal goes straight from the class to the initializer.
        assert (currentMethods.peek() == null || callerEffect.equals(effStack.peek()));

        if (!genericEffect.LE(targetEffect, callerEffect)) {
            checker.report(Result.failure("call.invalid.super.effect", targetEffect, callerEffect), node);
            if (debugSpew) {
                System.err.println("Issuing error for node: " + node);
            }
        }
        if (debugSpew) {
            System.err.println(
                    "Successfully finished main non-recursive checkinv of invocation " + node);
        }

        return super.visitMethodInvocation(node, p);
    }
    
    @Override
    public Void visitMethod(MethodTree node, Void p) {
        // TODO: If the type we're in is a polymorphic (over effect qualifiers) type, the receiver must be @PolyIO.
        //       Otherwise a "non-polymorphic" method of a polymorphic type could be called on a IO instance, which then
        //       gets a NoIO reference to itself (unsound!) that it can then pass off elsewhere (dangerous!).  So all
        //       receivers in methods of a @PolyIOType must be @PolyIO.
        // TODO: What do we do then about classes that inherit from a concrete instantiation?  If it subclasses a NoIO
        //       instantiation, all is well.  If it subclasses a IO instantiation, then the receivers should probably
        //       be @IO in both new and override methods, so calls to polymorphic methods of the parent class will work
        //       correctly.  In which case for proving anything, the qualifier on sublasses of IO instantiations would
        //       always have to be @IO... Need to write down |- t for this system!  And the judgments for method overrides
        //       and inheritance!  Those are actually the hardest part of the system.

        ExecutableElement methElt = TreeUtils.elementFromDeclaration(node);
        if (debugSpew) {
            System.err.println("\nVisiting method " + methElt);
        }

        // Check for conflicting (multiple) annotations
        assert (methElt != null);
        
        //Ekta: Need to enumerate collection of valid effects and return the valid effect if methElt matches the valid effect from the list. 
        ArrayList<Class<? extends Annotation>> validEffects = genericEffectHeirarchy.getValidEffects();
        AnnotationMirror annotatedEffect;
        
        for(Class<? extends Annotation> OkEffect: validEffects){
        	annotatedEffect=atypeFactory.getDeclAnnotation(methElt, OkEffect);
        	
        	TypeElement targetClassElt = (TypeElement) methElt.getEnclosingElement(); //Get the annotation on the class

           /* if (annotatedEffect != null && atypeFactory.isSameEffect(targetClassElt)) {
                checker.report(Result.warning("effects.redundant.iotype"), node);
            }*/

            // TODO: Report an error for polymorphic method bodies??? Until we fix the receiver defaults, it won't really be correct
            @SuppressWarnings("unused") // call has side-effects
            /*GenericEffect.EffectRange range =
                    atypeFactory.findInheritedEffectRange(
                            ((TypeElement) methElt.getEnclosingElement()), methElt, true, node);*/
          
            atypeFactory.checkEffectOverrid((TypeElement)methElt.getEnclosingElement(), methElt, true, node);
           
            if (annotatedEffect == null) {
                atypeFactory
                        .fromElement(methElt)
                        .addAnnotation(atypeFactory.getDeclaredEffect(methElt).getAnnot());
            }
        
        }
        	
        /*AnnotationMirror targetIOP = atypeFactory.getDeclAnnotation(methElt, IOEffect.class);
        AnnotationMirror targetNoIOP = atypeFactory.getDeclAnnotation(methElt, NoIOEffect.class);*/
        
        
        // We hang onto the current method here for ease.  We back up the old
        // current method because this code is reentrant when we traverse methods of an inner class
        currentMethods.push(node);
        effStack.push(atypeFactory.getDeclaredEffect(methElt));
        if (debugSpew) {
            System.err.println(
                    "Pushing " + effStack.peek() + " onto the stack when checking " + methElt);
        }

        Void ret = super.visitMethod(node, p);
        currentMethods.pop();
        effStack.pop();
        return ret;
    }
    
    @Override
    public Void visitMemberSelect(MemberSelectTree node, Void p) {
        //TODO: Same effect checks as for methods
        return super.visitMemberSelect(node, p);
    }
    
    @Override
    public Void visitClass(ClassTree node, Void p) {
        // TODO: Check constraints on this class decl vs. parent class decl., and interfaces
        // TODO: This has to wait for now: maybe this will be easier with the isValidUse on the TypeFactory
        // AnnotatedTypeMirror.AnnotatedDeclaredType atype = atypeFactory.fromClass(node);

        // Push a null method and IO effect onto the stack for static field initialization
        // TODO: Figure out if this is noIO! For static data, almost certainly,
        // but for statically initialized instance fields, I'm assuming those
        // are implicitly moved into each constructor, which must then be @IO
        currentMethods.push(null);// static int x=dosomething();
        //effStack.push(new MainEffect(IOEffect.class));
        effStack.push(genericEffect);
        Void ret = super.visitClass(node, p);
        currentMethods.pop();
        effStack.pop();
        return ret;
    }
 
    @Override
    public Void visitNewClass(NewClassTree node, Void p) {
        if (debugSpew) {
            System.err.println("For constructor " + node + " in " + currentMethods.peek().getName());
        }

        // Target method annotations
        ExecutableElement methodElt = TreeUtils.elementFromUse(node);//ExecutableElement: This gets the inherited methods from the base class of the method being analyzed
        if (debugSpew) {
            System.err.println("methodElt found");
        }

        MethodTree callerTree = TreeUtils.enclosingMethod(getCurrentPath());
        if (callerTree == null) {
            // Static initializer; let's assume this is safe to have the IO effect
            if (debugSpew) {
                System.err.println("No enclosing method: likely static initializer");
            }
            return super.visitNewClass(node, p);
        }
        if (debugSpew) {
            System.err.println("callerTree found");
        }

        ExecutableElement callerElt = TreeUtils.elementFromDeclaration(callerTree);// What annotations has been applied
        if (debugSpew) {
            System.err.println("callerElt found");
        }

        Class<? extends Annotation> targetEffect = atypeFactory.getDeclaredEffect(methodElt);
        Class<? extends Annotation> callerEffect = atypeFactory.getDeclaredEffect(callerElt);
        // Field initializers inside anonymous inner classes show up with a null current-method ---
        // the traversal goes straight from the class to the initializer.
        assert (currentMethods.peek() == null || callerEffect.equals(effStack.peek()));

        if (!genericEffect.LE(targetEffect, callerEffect)) {
            checker.report(Result.failure("constructor.call.invalid", targetEffect, callerEffect), node);
            if (debugSpew) {
                System.err.println("Issuing error for node: " + node);
            }
        }
        if (debugSpew) {
            System.err.println(
                    "Successfully finished main non-recursive checkinv of invocation " + node);
        }

        return super.visitNewClass(node, p);
    }
}
