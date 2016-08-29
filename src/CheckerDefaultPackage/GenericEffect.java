package CheckerDefaultPackage;

import java.lang.annotation.Annotation;

public interface GenericEffect {

	 boolean LE(Class<? extends Annotation> left, Class<? extends Annotation> right);
	 Class<? extends Annotation> min(Class<? extends Annotation> l, Class<? extends Annotation> r);
	/* boolean isSub();
	 boolean isSuper();
	 boolean equals(Class<? extends Annotation> e);*/
	
	 class EffectRange {
		 Class<? extends Annotation> min, max;

	        public EffectRange(Class<? extends Annotation> min, Class<? extends Annotation> max) {
	            assert (min != null || max != null);
	            // If one is null, fill in with the other
	            this.min = (min != null ? min : max);
	            this.max = (max != null ? max : min);
	        }
	    }
	 
	/*Class<? extends Annotation> getAnnot();*/

}
