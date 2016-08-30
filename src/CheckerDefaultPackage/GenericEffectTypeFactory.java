package CheckerDefaultPackage;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotationClassLoader;
import com.sun.source.tree.Tree;

import testing.EffectHierarchy;
import testing.MainEffect;


public class GenericEffectTypeFactory extends BaseAnnotatedTypeFactory {

	protected final boolean debugSpew;
	private GenericEffect genericEffect;
	private GenericEffectHeirarchy genericEffectHeirarchy;

	public GenericEffectTypeFactory(BaseTypeChecker checker, boolean spew) {
		// use true to enable flow inference, false to disable it
		super(checker, false);
		
		//For testing IO Effect Checker inside Generic Effect Checker
		genericEffect=new MainEffect();
		genericEffectHeirarchy=new EffectHierarchy();
		
		debugSpew = spew;
		this.postInit();
	}
	
	/**
	 * For accessing effects from the command line, commented out for code as instantiating effects in the constructors and not 
	 * accessing effects from command line
	 */
	/*@Override
	protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
		AnnotationClassLoader loader = new AnnotationClassLoader(checker);

		Set<Class<? extends Annotation>> qualSet = new HashSet<Class<? extends Annotation>>();

		String qualNames = checker.getOption("quals");
		String qualDirectories = checker.getOption("qualDirs");

		if (qualNames == null && qualDirectories == null) {
			checker.userErrorAbort("SubtypingChecker: missing required option. Use -Aquals or -AqualDirs");
			throw new Error("This can't happen"); // dead code
		}

		// load individually named qualifiers
		if (qualNames != null) {
			for (String qualName : qualNames.split(",")) {
				qualSet.add(loader.loadExternalAnnotationClass(qualName));
			}
		}

		// load directories of qualifiers
		if (qualDirectories != null) {
			for (String dirName : qualDirectories.split(":")) {
				qualSet.addAll(loader.loadExternalAnnotationClassesFromDirectory(dirName));
			}
		}

		// check for subtype meta-annotation
		for (Class<? extends Annotation> qual : qualSet) {
			Annotation subtypeOfAnnotation = qual.getAnnotation(SubtypeOf.class);
			if (subtypeOfAnnotation != null) {
				for (Class<? extends Annotation> superqual : qual.getAnnotation(SubtypeOf.class).value()) {
					if (!qualSet.contains(superqual)) {
						checker.userErrorAbort("SubtypingChecker: qualifier " + qual
								+ " was specified via -Aquals but its super-qualifier " + superqual + " was not");
					}
				}
			}
		}

		return qualSet;
	}*/

	/**
	 * Method to check if override method's effect is valid override 
	 * 
	 * @param overrider : Method in the subclass which is overriding the method of superclass
	 * @param parentType : Parent type, whose method is being overridden
	 * @return
	 * 			Overridden method :  as Executable element
	 * 			null			  :	 if matching overridden method not found in Parent type
	 * 		
	 */
	public ExecutableElement findJavaOverride(ExecutableElement overrider, TypeMirror parentType) {
		if (parentType.getKind() != TypeKind.NONE) {
			if (debugSpew) {
				System.err.println("Searching for overridden methods from " + parentType);
			}

			TypeElement overriderClass = (TypeElement) overrider.getEnclosingElement();
			TypeElement elem = (TypeElement) ((DeclaredType) parentType).asElement();
			if (debugSpew) {
				System.err.println("necessary TypeElements acquired: " + elem);
			}

			for (Element e : elem.getEnclosedElements()) {
				if (debugSpew) {
					System.err.println("Considering element " + e);
				}
				if (e.getKind() == ElementKind.METHOD || e.getKind() == ElementKind.CONSTRUCTOR) {
					ExecutableElement ex = (ExecutableElement) e;
					boolean overrides = elements.overrides(overrider, ex, overriderClass);
					if (overrides) {
						return ex;
					}
				}
			}
			if (debugSpew) {
				System.err.println("Done considering elements of " + parentType);
			}
		}
		return null;
	}

	/**
	 * Returns the Declared Effect on the passed method as parameter
	 * @param methodElt : Method for which declared effect is to be returned  
	 * @return 	
	 * 			declared effect				: if methodElt is annotated with a valid effect
	 * 			bottomMostEffectInLattice	: otherwise, bottom most effect of lattice
	 * 
	 */
	public Class<? extends Annotation> getDeclaredEffect(ExecutableElement methodElt) {

		if (debugSpew) {
			System.err.println("begin mayHaveIOEffect(" + methodElt + ")");
		}

		ArrayList<Class<? extends Annotation>> validEffects = genericEffectHeirarchy.getValidEffects();
		AnnotationMirror annotatedEffect = null;

		for (Class<? extends Annotation> OkEffect : validEffects) {
			annotatedEffect = getDeclAnnotation(methodElt, OkEffect);
			if (annotatedEffect != null) {
				if (debugSpew) {
					System.err.println("Method marked @" + annotatedEffect);
				}
				return OkEffect;
			}
		}
		return genericEffectHeirarchy.getBottomMostEffectInLattice();
	}

	/**
	 * Looks for invalid overrides, (cases where a method override declares a
	 * larger/higher effect than a method it overrides/implements)
	 * 
	 * @param declaringType 		: Class containing the overriding method
	 * @param overridingMethod 		: Overriding method in declaringType
	 * @param issueConflictWarning 	: true if warning should be issued 
	 * @param errorNode 			: node to check for errors
	 */
	public void checkEffectOverrid(TypeElement declaringType, ExecutableElement overridingMethod,
			boolean issueConflictWarning, Tree errorNode) {
		assert (declaringType != null);

		// Get the overriding method annotations
		// Iterate over all of its subtypes
		// For each subtype, that has its own implementation or declaration of
		// the input method:
		// Check that the effect of the override <= the declared effect of the
		// origin.

		// There are two sets of subtypes to traverse:
		// 1. Chain of Parent classes -> terminating in Object
		// 2. Set of interfaces the class implements.

		Class<? extends Annotation> overridingEffect = getDeclaredEffect(overridingMethod);

		// Chain of parent classes
		TypeMirror superclass = declaringType.getSuperclass();
		while (superclass != null && superclass.getKind() != TypeKind.NONE) {
			ExecutableElement overrides = findJavaOverride(overridingMethod, superclass);
			if (overrides != null) {
				Class<? extends Annotation> superClassEffect = getDeclaredEffect(overrides);
				if(!genericEffect.LE(overridingEffect, superClassEffect)){
					checker.report(Result.failure("override.effect.invalid", overridingMethod, declaringType, overrides,
							superclass), errorNode);
				}
			}

			DeclaredType decl = (DeclaredType) superclass;
			superclass = ((TypeElement) decl.asElement()).getSuperclass();
		}

		// Set of interfaces
		List<? extends TypeMirror> listOfInterfaces = declaringType.getInterfaces();
		if (listOfInterfaces != null) {
			for (TypeMirror implementedInterface : listOfInterfaces) {
				if (implementedInterface.getKind() != TypeKind.NONE) {
					ExecutableElement overrides = findJavaOverride(overridingMethod, implementedInterface);
					if (overrides != null) {
						Class<? extends Annotation> interfaceEffect = getDeclaredEffect(overrides);
						if (!genericEffect.LE(overridingEffect, interfaceEffect)
								&& issueConflictWarning) {
							checker.report(Result.failure("override.effect.invalid", overridingMethod, declaringType,
									overrides, implementedInterface), errorNode);
						}
					}
				}
			}
		}

	}
}
