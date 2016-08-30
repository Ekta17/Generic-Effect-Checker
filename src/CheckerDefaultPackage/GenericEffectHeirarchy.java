package CheckerDefaultPackage;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

public interface GenericEffectHeirarchy {

	//Get the collection of valid effects. For example, for IO EFfect checker, IOEffect and NoIOEffect are valid effects 
	ArrayList<Class<? extends Annotation>> getValidEffects();

	//Get the Top Most Effect of Lattice
    public Class<? extends Annotation> getTopMostEffectInLattice();

	//Get the Bottom Most Effect of Lattice
    public Class<? extends Annotation> getBottomMostEffectInLattice();
}
