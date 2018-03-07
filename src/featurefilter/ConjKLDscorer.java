package featurefilter;

import learnsdd.Model;
import data.Data;
import logic.ConjunctionOfLiterals;
import logic.BooleanFormula;
import utilities.ConjProbCalculator;
import utilities.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * Scores a potential feature based on the KL divergence.
 * The KLD measures the the difference between distributions,
 * in this case the distribution of the feature in the data vs the distribution of the feature in the model.
 * If the feature behaves very different in the model than in the data, then it is probably an interesting feature.
 *
 * This class assumes conjunctive binary features. For this kind of features, it is easy to calculate the model probability.
 * 
 * @author jessa
 *
 */
public class ConjKLDscorer implements FeatureScorer {

	HashMap<HashSet<Integer>, ArrayList<Pair<Double, Double>>> modelProbs;

	@Override
	public void init() {
		modelProbs = new HashMap<HashSet<Integer>, ArrayList<Pair<Double, Double>>>();
	}

	@Override
	public double score(BooleanFormula f, Model model) {
		ConjunctionOfLiterals feature = (ConjunctionOfLiterals) f;
		double modelProb = getModelProb(feature, model);
		double dataProb = getDataProb(feature, model.getTrainingData());
		double score = calculateKLD(modelProb, dataProb);
		f.setLastKLD(score);

		return score;
	}
	


	@Override
	public String getName() {
		return "kld";
	}

	private double calculateKLD(double modelProb, double dataProb) {
		double Q1 = modelProb;
		double Q2 = 1 - Q1;
		double P1 = dataProb;
		double P2 = 1 - P1;
		return KLDsumTerm(P1, Q1) + KLDsumTerm(P2, Q2);
	}

	private double KLDsumTerm(double P, double Q) {
		if (P == 0)
			return 0;
		if (Q == 0)
			return Double.POSITIVE_INFINITY;
		return P * Math.log(P / Q);
	}

	private double getDataProb(BooleanFormula feature, Data trainingData) {
		if(feature.getProbabilityInData()<0){
			double nbInstances = trainingData.getNbInstances();
			double nbOccurences = trainingData.getCount(feature);
			feature.setProbabilityInData(nbOccurences / nbInstances);
		}
		return feature.getProbabilityInData();
	}

	private double getModelProb(ConjunctionOfLiterals feature,
                                Model model) {
		Pair<Boolean, Double> p = getModelProbIfCheap(feature, model);
		if (p.left())
			return p.right();
		
		Pair<HashSet<Integer>, Integer> split = getSubs(feature.getElements()).iterator().next();
		ConjProbCalculator probCalc = new ConjProbCalculator(model);
		ArrayList<Pair<Double, Double>> probs = probCalc
				.getConjunctionProbabilities(split.left());
		modelProbs.put(split.left(), probs);

		if (split.right() < 0)
			return probs.get(-split.right()).right();
		return probs.get(split.right()).left();
	}
	
	private Pair<Boolean, Double> getModelProbIfCheap(ConjunctionOfLiterals feature,
                                                      Model model) {
		for (Pair<HashSet<Integer>, Integer> sub : getSubs(feature
				.getElements())) {
			if (modelProbs.containsKey(sub.left())) {
				if (sub.right() < 0)
					return new Pair<Boolean, Double>(true,modelProbs.get(sub.left()).get(-sub.right()).right());
				return new Pair<Boolean, Double>(true, modelProbs.get(sub.left()).get(sub.right()).left());
			}
		}
		return new Pair<Boolean, Double>(false, 0.0);
	}

	private Set<Pair<HashSet<Integer>, Integer>> getSubs(Set<Integer> set) {
		Set<Pair<HashSet<Integer>, Integer>> subs = new HashSet<Pair<HashSet<Integer>, Integer>>();
		for (int f : set) {
			HashSet<Integer> subset = new HashSet<Integer>(set);
			subset.remove(f);
			subs.add(new Pair<HashSet<Integer>, Integer>(subset, f));
		}
		return subs;
	}

}
