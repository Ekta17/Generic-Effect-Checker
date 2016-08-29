package testing;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

import CheckerDefaultPackage.GenericEffectHeirarchy;
import CheckerDefaultPackage.qual.IOEffect;
import CheckerDefaultPackage.qual.NoIOEffect;


public class EffectHierarchy implements GenericEffectHeirarchy{

	@Override
	public ArrayList<Class<? extends Annotation>> getValidEffects() {
		
		ArrayList<Class<? extends Annotation>> listOfEffects=new ArrayList<>();
		listOfEffects.add(NoIOEffect.class);
		listOfEffects.add(IOEffect.class);
		
		return listOfEffects;
	}

	@Override
	public Class<? extends Annotation> getTopMostEffectInLattice() {
		return IOEffect.class;
	}

	@Override
	public Class<? extends Annotation> getBottomMostEffectInLattice() {
		return NoIOEffect.class;
	}

}
