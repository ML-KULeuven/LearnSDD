package utilities;

import learnsdd.Model;
import sdd.WmcManager;

import java.util.ArrayList;
import java.util.Set;

/**
 * Calculates the probabilities of all features being true together with the given feature instantiations. (necessary for kld filter)
 */
public class ConjProbCalculator {
	
	Model model;
	double Z;
	
	public ConjProbCalculator(Model model) {
		this.model=model;
		WmcManager wmc = new WmcManager(model.getTheory().getSdd(), true);
		for(int f=1; f<=model.getNbOfFeatures();f++){
			wmc.setLiteralWeight(f, model.getWeight(f));
		}
		Z = wmc.propagate();
		wmc.free();
	}
	
	/**
	 * 
	 * @param features
	 * @return
	 */
	public ArrayList<Pair<Double,Double>> getConjunctionProbabilities(Set<Integer> features){
		WmcManager wmc = new WmcManager(model.getTheory().getSdd(), true);
		for(int f=1; f<=model.getNbOfFeatures();f++){
			wmc.setLiteralWeight(f, model.getWeight(f));
		}
		for(int feature: features)
			wmc.setLiteralWeight(-feature, wmc.getZeroWeight());
		double Z2 = wmc.propagate();
		
		double probFeature = Math.exp(Z2-Z);
		
		ArrayList<Pair<Double,Double>> probs = new ArrayList<Pair<Double,Double>>();
		probs.add(new Pair<Double,Double>(0.0,0.0));
		for(int f=1; f<=model.getNbOfFeatures();f++){
			double condProb = Math.exp(wmc.getProbability(f));
			probs.add(new Pair<Double,Double>(probFeature*condProb, probFeature*(1-condProb)));
		}
		wmc.free();
		
		return probs;
	}
	
}
