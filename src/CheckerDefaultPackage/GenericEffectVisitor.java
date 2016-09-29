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

import testing.EffectHierarchy;
import testing.MainEffect;

public class GenericEffectVisitor  extends BaseTypeVisitor<GenericEffectTypeFactory>{

	protected final boolean debugSpew;
	private GenericEffect genericEffect;
	private GenericEffectHeirarchy genericEffectHeirarchy;

    // effStack and currentMethods should always be the same size.
    protected final Stack<Class<? extends Annotation>> effStack;
    protected final Stack<MethodTree> currentMethods;

    public GenericEffectVisitor(BaseTypeChecker checker) {
        super(checker);
        debugSpew = checker.getLintOption("debugSpew", false);
        if (debugSpew) {
            System.err.println("Running IOEffectVisitor");
        }
        effStack = new Stack<Class<? extends Annotation>>();
        currentMethods = new Stack<MethodTree>();
        
        //For testing IO Effect Checker inside Generic Effect Checker
        genericEffect=new MainEffect();
        genericEffectHeirarchy=new EffectHierarchy();
    }
    
    //Method to instantiate the factory class for the checker
    @Override
    protected GenericEffectTypeFactory createTypeFactory() {
        return new GenericEffectTypeFactory(checker, debugSpew);
    }
    
    @Override
    protected void checkMethodInvocability(
            AnnotatedExecutableType method, MethodInvocationTree node) {
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
    
    // Method to check that the invoked effect is <= permitted effect (effStack.peek())
    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        if (debugSpew) {
            System.err.println("For invocation " + node + " in " + currentMethods.peek().getName());
        }

        // Target method annotations
        ExecutableElement methodElt = TreeUtils.elementFromUse(node);
        if (debugSpew) {
            System.err.println("methodElt found");
        }

        MethodTree callerTree = TreeUtils.enclosingMethod(getCurrentPath());
        if (callerTree == null) {
            if (debugSpew) {
                System.err.println("No enclosing method: likely static initializer");
            }
            return super.visitMethodInvocation(node, p);
        }
        if (debugSpew) {
            System.err.println("callerTree found");
        }

        ExecutableElement callerElt = TreeUtils.elementFromDeclaration(callerTree);
        if (debugSpew) {
            System.err.println("callerElt found");
        }

        Class<? extends Annotation> targetEffect = atypeFactory.getDeclaredEffect(methodElt);
        Class<? extends Annotation> callerEffect = atypeFactory.getDeclaredEffect(callerElt);
        
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
       
    	ExecutableElement methElt = TreeUtils.elementFromDeclaration(node);
        if (debugSpew) {
            System.err.println("\nVisiting method " + methElt);
        }

        assert (methElt != null);
        
        ArrayList<Class<? extends Annotation>> validEffects = genericEffectHeirarchy.getValidEffects();
        AnnotationMirror annotatedEffect;
        
        for(Class<? extends Annotation> OkEffect: validEffects){
        	annotatedEffect=atypeFactory.getDeclAnnotation(methElt, OkEffect);
        	
            ((GenericEffectTypeFactory)atypeFactory).checkEffectOverrid((TypeElement)(methElt.getEnclosingElement()), methElt, true, node);
           
            if (annotatedEffect == null) {
                atypeFactory
                        .fromElement(methElt)
                        .addAnnotation(atypeFactory.getDeclaredEffect(methElt));
            }
        
        }
        	
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
        
    	currentMethods.push(null);
        effStack.push(genericEffectHeirarchy.getBottomMostEffectInLattice());
        Void ret = super.visitClass(node, p);
        currentMethods.pop();
        effStack.pop();
        return ret;
    }
 
    /*
     * Method to check if the constructor call is made from a valid context 
     */
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
