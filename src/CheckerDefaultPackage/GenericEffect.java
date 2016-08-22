package CheckerDefaultPackage;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

public interface GenericEffect {

	 boolean LE(Class<? extends Annotation> left, Class<? extends Annotation> right);
	 GenericEffect min(GenericEffect l, GenericEffect r);
	 boolean isSub();
	 boolean isSuper();
	 boolean equals(GenericEffect e);
	
	 class EffectRange {
		 GenericEffect min, max;

	        public EffectRange(GenericEffect min, GenericEffect max) {
	            assert (min != null || max != null);
	            // If one is null, fill in with the other
	            this.min = (min != null ? min : max);
	            this.max = (max != null ? max : min);
	        }
	    }
	 
	Class<? extends Annotation> getAnnot();

}
