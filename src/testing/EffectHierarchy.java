package testing;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

import CheckerDefaultPackage.GenericEffectHeirarchy;
import CheckerDefaultPackage.qual.IOEffect;
import CheckerDefaultPackage.qual.NoIOEffect;
/**
 * Test Class to test IO Effect Checker inside Generic Effect Checker
 *
 * Builds Effect Hierarchy for IO Effect Checker
 */

public class EffectHierarchy implements GenericEffectHeirarchy{

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
