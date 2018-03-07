package featurefilter;


import learnsdd.Model;
import logic.BooleanFormula;

import java.util.List;

/**
 * Usually way too many features are generated and not all of them should be tried.
 * The filter only keeps a subset of the features that should be tried.
 */
public interface FeatureFilter{
	
	public List<? extends BooleanFormula> filter(List<? extends BooleanFormula> candidates, Model model);

}
