package featurefilter;

import learnsdd.Model;
import logic.BooleanFormula;

public interface FeatureScorer {
	
	double score(BooleanFormula feature, Model model);

	String getName();

	void init();

}
