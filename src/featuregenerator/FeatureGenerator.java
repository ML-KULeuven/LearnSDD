package featuregenerator;

import learnsdd.Model;
import logic.BooleanFormula;

import java.util.List;

/**
 * Generate features based on the current model.
 */
public interface FeatureGenerator{
	
	public List<? extends BooleanFormula> generate(Model model);


	public void informChosenFeature(
			BooleanFormula chosenFeatureCol);

	public List<? extends BooleanFormula> getNewFeatures();

	public List<? extends BooleanFormula> getDiscardedFeatures();
}
