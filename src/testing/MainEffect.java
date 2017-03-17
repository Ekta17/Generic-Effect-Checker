package testing;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

import CheckerDefaultPackage.GenericEffect;
import CheckerDefaultPackage.qual.IOEffect;
import CheckerDefaultPackage.qual.NoIOEffect;

/**
 * Test Class to test IO Effect Checker inside Generic Effect Checker
 *
 * Creates and checks relationship among the valid effects of IO Effect Checker
 */

public final class MainEffect implements GenericEffect {

	/**
	 * Method to check Less than equal to Effect
	 * 
	 * @param left : Left Effect 
	 * @param right: Right Effect
	 * @return boolean
	 * 		true	: if bottom effect is left effect and is equal to NoIOEffect OR 
	 * 				  if top effect is right effect and is equal to IOEffect OR
	 * 				  if left effect and right effect are the same
	 * 		
	 * 		false	: otherwise
	 */
	
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

	/**
	 * Method to get minimum of (l, r)
	 * 
	 * @param l : left effect
	 * @param r : right effect
	 * @return minimum(l,r)
	 */
	
	@Override
	public Class<? extends Annotation> min(Class<? extends Annotation> l, Class<? extends Annotation> r) {
		if (LE(l, r)) {
            return l;
        } else {
            return r;
        }
	}
	
	@Override
    public boolean equals(Object o) {
        if (o instanceof MainEffect) {
            return this.equals((MainEffect) o);
        } else {
            return super.equals(o);
        }
    }
	
	/**
	 * Get the collection of valid effects. 
	 * For IO EFfect checker:
	 * 	Valid Effects: 
	 * 					IOEffect, and 
	 * 					NoIOEffect
	 */
	
	@Override
	public ArrayList<Class<? extends Annotation>> getValidEffects() {
		
		ArrayList<Class<? extends Annotation>> listOfEffects=new ArrayList<>();
		listOfEffects.add(NoIOEffect.class);
		listOfEffects.add(IOEffect.class);
		
		return listOfEffects;
	}

	/**
	 * Get the Top Most Effect of Lattice.
	 * For IO EFfect checker:
	 * Top Most Effect of Lattice:	IOEffect
	 * 
	 */
	
	@Override
	public Class<? extends Annotation> getTopMostEffectInLattice() {
		return IOEffect.class;
	}

	/**
	 * Get the Bottom Most Effect of Lattice.
	 * For IO EFfect checker:
	 * Bottom Most Effect of Lattice:	NoIOEffect
	 * 
	 */
	
	@Override
	public Class<? extends Annotation> getBottomMostEffectInLattice() {
		return NoIOEffect.class;
	}
}
