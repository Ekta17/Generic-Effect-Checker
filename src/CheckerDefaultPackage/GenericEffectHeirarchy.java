package CheckerDefaultPackage;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

public interface GenericEffectHeirarchy {

	//Collection of valid effects. For example, for IO EFfect checker, IOEffect and NoIOEffect are valid effects 
	//Ekta: should this be set instead of ArrayList?
	ArrayList<Class<? extends Annotation>> getValidEffects();
	
	//Methods from Effect Hierarchy
	
	/**
     * Returns the greatest lower bound for the Effects a1 and a2.
     *
     * The two Effects have to be from the same Effect hierarchy. Otherwise,
     * null will be returned.
     *
     * @param a1 first annotation
     * @param a2 second annotation
     * @return greatest lower bound of the two annotations
     *//*
    public abstract AnnotationMirror greatestLowerBound(AnnotationMirror a1, AnnotationMirror a2);*/
   /* 
    *//**
     * Return the top Effect for the given Effect, that is, the Effect
     * that is a super effect of start but no further super effect exist.
     *//*
    public abstract AnnotationMirror getTopAnnotation(AnnotationMirror start);*/

    /**
     * Return the bottom for the given Effect, that is, the Effect that is a
     * sub Effect of start but no further sub Effect exist.
     *//*
    public abstract AnnotationMirror getBottomAnnotation(AnnotationMirror start);*/

  /*  *//**
     * @return  the top (ultimate super) Effects in the type system
     *//*
    public abstract Set<? extends AnnotationMirror> getTopAnnotations();*/
    
   /* *//**
     * @return the bottom Effect in the hierarchy
     *//*
    public abstract Set<? extends AnnotationMirror> getBottomAnnotations();
    */

    /**
     * Tests whether rhs is a sub-Effect of lhs, according to the type
     * Effect hierarchy. This checks only the Effects, not the Java type.
     *
     * @return true iff rhs is a sub Effect of lhs
     *//*
    public abstract boolean isSubtype(Class<? extends Annotation> rhs, Class<? extends Annotation> lhs);*/
    
    //Ekta 
    
    public Class<? extends Annotation> getTopMostEffectInLattice();
    public Class<? extends Annotation> getBottomMostEffectInLattice();
}
