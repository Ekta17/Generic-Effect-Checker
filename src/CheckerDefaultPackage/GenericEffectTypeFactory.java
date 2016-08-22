package CheckerDefaultPackage;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotationClassLoader;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ElementUtils;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;

public class GenericEffectTypeFactory extends BaseAnnotatedTypeFactory {

	protected final boolean debugSpew;
	private GenericEffect genericEffect;
	private GenericEffectHeirarchy genericEffectHeirarchy;

	public GenericEffectTypeFactory(BaseTypeChecker checker, boolean spew) {
		// use true to enable flow inference, false to disable it
		super(checker, false);

		debugSpew = spew;
		this.postInit();
	}

	@Override
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
	}

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

	// Ekta: Is it possible to return GenericEffect instead of Class<? extends
	// Annotation>
	// like convert Class<? extends Annotation> to GenericEffect like a wrapper
	// or something?
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

		/*
		 * AnnotationMirror targetIOP = getDeclAnnotation(methodElt,
		 * IOEffect.class); AnnotationMirror targetNoIOP =
		 * getDeclAnnotation(methodElt, NoIOEffect.class);
		 */

		/*
		 * TypeElement targetClassElt = (TypeElement)
		 * methodElt.getEnclosingElement();
		 * 
		 * if (debugSpew) { System.err.println("targetClassElt found"); }
		 */

		// Short-circuit if the method is explicitly annotated

		/*
		 * if (targetNoIOP != null) { if (debugSpew) {
		 * System.err.println("Method marked @NoIOEffect"); } return new
		 * MainEffect(NoIOEffect.class); } else if (targetIOP != null) { if
		 * (debugSpew) { System.err.println("Method marked @IOEffect"); } return
		 * new MainEffect(IOEffect.class); }
		 */

		// The method is not explicitly annotated, so check class and package
		// annotations,
		// and supertype effects if in an anonymous inner class

		/*
		 * if (isIOType(targetClassElt)) { // Already checked, no
		 * explicit @NoIOEffect annotation return new
		 * MainEffect(IOEffect.class); }
		 */
		// Anonymous inner types should just get the effect of the parent by
		// default, rather than annotating every instance. Unless it's
		// implementing a polymorphic supertype, in which case we still want the
		// developer to be explicit.
		/*
		 * if (isAnonymousType(targetClassElt)) { boolean
		 * canInheritParentEffects = true; // Refine this for polymorphic
		 * parents //DeclaredType directSuper = (DeclaredType)
		 * targetClassElt.getSuperclass(); //TypeElement superElt =
		 * (TypeElement) directSuper.asElement(); // Anonymous subtypes of
		 * polymorphic classes other than object can't inherit
		 * 
		 * if (canInheritParentEffects) { GenericEffectInterface.EffectRange r =
		 * findInheritedEffectRange(targetClassElt, methodElt); return (r !=
		 * null ? genericEffect.min(r.min, r.max) : new
		 * MainEffect(NoIOEffect.class) genericEffect.greatestLowerBound(a1,
		 * a2)); } }
		 */

		return /* new MainEffect(NoIOEffect.class) */genericEffectHeirarchy.getBottomMostEffectInLattice();
	}

	/*public void findInheritedEffectRange(TypeElement declaringType,
			ExecutableElement overridingMethod) {
		findInheritedEffectRange(declaringType, overridingMethod, false, null);
	}*/

	public void checkEffectOverrid(TypeElement declaringType, ExecutableElement overridingMethod,
			boolean issueConflictWarning, Tree errorNode) {
		assert (declaringType != null);

		ExecutableElement superOverride = null;
		ExecutableElement subOverride = null;
		/*
		 * ExecutableElement io_override = null; ExecutableElement noIO_override
		 * = null;
		 */

		// We must account for explicit annotation, type declaration
		// annotations, and package annotations

		// Ekta :Not Sure of this code's Validity

		for (Class<? extends Annotation> validEffect : genericEffectHeirarchy.getValidEffects()) {
			AnnotationMirror declaredAnnotation = getDeclAnnotation(overridingMethod, validEffect);

			boolean isSuperEffect = (declaredAnnotation != null
			/* || isIOType(declaringType) */) && getDeclAnnotation(overridingMethod,
					genericEffectHeirarchy.getBottomAnnotation(declaredAnnotation)) == null;

			// TODO: We must account for @IO and @AlwaysNoIO annotations for
			// extends
			// and implements clauses, and do the proper substitution of @Poly
			// effects and quals!
			// List<? extends TypeMirror> interfaces =
			// declaringType.getInterfaces();
			TypeMirror superclass = declaringType.getSuperclass();
			while (superclass != null && superclass.getKind() != TypeKind.NONE) {
				ExecutableElement overrides = findJavaOverride(overridingMethod, superclass);
				if (overrides != null) {
					GenericEffect eff = getDeclaredEffect(overrides);
					assert (eff != null);
					// Ekta: here instead of using decalredAnnotation it should
					// be Class<? extends Annotation> validEffect
					if (/* eff.isNoIO() */ eff.equals(genericEffect.getBottomAnnotation(declaredAnnotation))) {
						// found a noIO override
						subOverride = overrides;
						if (isSuperEffect && issueConflictWarning) {
							checker.report(Result.failure("override.effect.invalid", overridingMethod, declaringType,
									subOverride, superclass), errorNode);
						}
					} else if (eff.isSuper()) {
						// found a io override
						superOverride = overrides;
					}
				}
				DeclaredType decl = (DeclaredType) superclass;
				superclass = ((TypeElement) decl.asElement()).getSuperclass();
			}

			AnnotatedTypeMirror.AnnotatedDeclaredType annoDecl = fromElement(declaringType);
			for (AnnotatedTypeMirror.AnnotatedDeclaredType ty : annoDecl.directSuperTypes()) {
				ExecutableElement overrides = findJavaOverride(overridingMethod, ty.getUnderlyingType());
				if (overrides != null) {
					GenericEffect eff = getDeclaredEffect(overrides);
					if (/* eff.isNoIO() */ eff.equals(genericEffect.getBottomAnnotation(declaredAnnotation))) {
						// found a noIO override
						subOverride = overrides;
						if (isSuperEffect && issueConflictWarning) {
							checker.report(Result.failure("override.effect.invalid", overridingMethod, declaringType,
									subOverride, ty), errorNode);
						}
					} else if (eff.isSuper()) {
						// found a io override
						superOverride = overrides;
					}
				}
			}

			// We don't need to issue warnings for inheriting from poly and a
			// concrete effect.
			if (superOverride != null && subOverride != null && issueConflictWarning) {
				// There may be more than two parent methods, but for now it's
				// enough to know there are at least 2 in conflict
				checker.report(
						Result.warning("override.effect.warning.inheritance", overridingMethod, declaringType,
								superOverride.toString(), superOverride.getEnclosingElement().asType().toString(),
								subOverride.toString(), subOverride.getEnclosingElement().asType().toString()),
						errorNode);
			}

			GenericEffect min = null;

			if (subOverride != null)
				min = new MainEffect(NoIOEffect.class);

			GenericEffect max = null;

			if (superOverride != null)
				max = new MainEffect(IOEffect.class);

			if (debugSpew) {
				System.err.println("Found " + declaringType + "." + overridingMethod + " to have inheritance pair ("
						+ min + "," + max + ")");
			}

			if (min == null && max == null) {
				return null;
			} else {
				return genericEffect.EffectRange(min, max);
			}

		}
	}
}
