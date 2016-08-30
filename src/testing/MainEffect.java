package testing;

import java.lang.annotation.Annotation;

import CheckerDefaultPackage.GenericEffect;
import CheckerDefaultPackage.qual.IOEffect;
import CheckerDefaultPackage.qual.NoIOEffect;


public final class MainEffect implements GenericEffect {

	/*private final Class<? extends Annotation> annotClass;

	public MainEffect(Class<? extends Annotation> cls) {
		assert (cls.equals(IOEffect.class) || cls.equals(NoIOEffect.class));
		annotClass = cls;
	}*/

	@Override
	public boolean LE(Class<? extends Annotation> left, Class<? extends Annotation> right) {
		assert (left != null && right != null);

		Class<? extends Annotation> leftEffect;
		Class<? extends Annotation> rightEffect;

		boolean leftBottom = (left.equals(NoIOEffect.class)) ? true : false;
		
		if (leftBottom)
			leftEffect = NoIOEffect.class;
		else
			leftEffect = IOEffect.class;

		boolean rightTop = (right.equals(IOEffect.class)) ? true : false;
		
		if (rightTop)
			rightEffect = IOEffect.class;
		else
			rightEffect = NoIOEffect.class;

		return leftBottom || rightTop || leftEffect.equals(rightEffect);
	}

	@Override
	public Class<? extends Annotation> min(Class<? extends Annotation> l, Class<? extends Annotation> r) {
		if (LE(l, r)) {
            return l;
        } else {
            return r;
        }
	}

	/*@SideEffectFree
    @Override
    public String toString() {
        return annotClass.getSimpleName();
    }*/
	
	@Override
    public boolean equals(Object o) {
        if (o instanceof MainEffect) {
            return this.equals((MainEffect) o);
        } else {
            return super.equals(o);
        }
    }

   /* @Pure
    @Override
    public int hashCode() {
        return 31 + annotClass.hashCode();
    }*/

}
