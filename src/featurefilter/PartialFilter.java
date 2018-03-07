package featurefilter;

import learnsdd.Model;
import logic.BooleanFormula;
import utilities.Pair;

import java.util.*;

/**
 * This filter works under the assumption that the feature score can only decrease.
 *
 * The filter keeps the features partially ordered in between iterations, only updating it where necessary to give the k best.
 * 
 * @author jessa
 *
 */
public class PartialFilter implements FeatureFilter {

    // Each element of the list 'scores' consist of a tuple of 3 values: (score, feature, scoreUpToDateFlag).
    // The list is ordered by decreasing score
    // The score-up-to-date flag is true if the score is the correct score calculated for the current model.
    // The flag is false if the score is an old score, calculated for an old model.
    // In that case we know that true value of the score is at most the stored score.
	private ArrayList<Pair<Double, Pair<? extends BooleanFormula, Boolean>>> scores;
	private int limit;

	private FeatureScorer scorer;

	public PartialFilter(int limit, FeatureScorer scorer) {
		this.limit = limit;
		this.scorer = scorer;

		scores = new ArrayList<Pair<Double, Pair<? extends BooleanFormula, Boolean>>>();
	}


    /**
     * Add the feature to the sorted list, sorted on the scores.
     * @param fetaure
     * @return
     */
	private Integer insertInScores(
			Pair<Double, Pair<? extends BooleanFormula, Boolean>> fetaure) {
		double dscore = fetaure.left();
		int lo = 0;
		int hi = scores.size();
		while (lo < hi) {
			// Key is in scores[lo..hi]
			int mid = lo + (hi - lo) / 2;
			double midscore = scores.get(mid).left();
			if (mid == scores.size())
				lo = mid;
			else if (dscore < midscore)
				lo = mid + 1;
			else
				hi = mid;
		}
		assert (lo == hi);
		scores.add(lo, fetaure);
		return lo;
	}


    /**
     * recalculate score and update it's place in the list.
     *
     * @param i
     * @param model
     * @return
     */
	private Double recalculateScore(int i, Model model) {
		Pair<Double, Pair<? extends BooleanFormula, Boolean>> score = scores
				.get(i);
		scores.remove(i);
		score.setLeft(scorer.score((BooleanFormula) score.right().left(), model));
		score.right().setRight(true);
		insertInScores(score);
		return score.left();
	}


    /**
     * Filter the feature and return the most promising subset for the given model
     * @param features
     * @param model
     * @return
     */
	@Override
	public List<? extends BooleanFormula> filter(
			List<? extends BooleanFormula> features,
			Model model) {

		init();

		HashSet<? extends BooleanFormula> newFeatures = findNewFeaturesAndRemoveIrrelevantFeatures(features);

		addNewFeatures(model, newFeatures);

		List<? extends BooleanFormula> filtered = findLimitBest(model);

		return filtered;
	}


    /**
     * Find the best features, updating the scores where necessary along the way
     * @param model
     * @return
     */
	private List<? extends BooleanFormula> findLimitBest(Model model) {
		List<BooleanFormula> filtered = new ArrayList<BooleanFormula>();
		int i = 0;
		while (i < limit && i < scores.size()) {

			Pair<Double, Pair<? extends BooleanFormula, Boolean>> score = scores
					.get(i);
			if (score.right().right()) {
				filtered.add(score.right().left());
				i++;
			} else {
				Double newScore = recalculateScore(i, model);
				if (newScore >= score.left()) {
					if (newScore > score.left())
						System.err.println("Weird: kld  has gotten better...");
					filtered.add(score.right().left());
					i++;
				}
			}
		}
		return filtered;
	}

    /**
     * Add new features to the sorted list
     * @param model
     * @param featureSet
     */
	private void addNewFeatures(
			Model model,
			HashSet<? extends BooleanFormula> featureSet) {
		// add new features
		for (BooleanFormula feature : featureSet) {
			double s = scorer.score(feature, model);
			insertInScores(new Pair<Double, Pair<? extends BooleanFormula, Boolean>>(
					s,
					new Pair<BooleanFormula, Boolean>(
							feature, true)));
		}
	}

    /**
     * Check for all the features in the list if they are still relevant (if they are still possible according to the feature generator)
     * Remove those which are not
     * Return the new relevant features which are not in the sorted list yet.
     * @param features
     * @return
     */
	private HashSet<? extends BooleanFormula> findNewFeaturesAndRemoveIrrelevantFeatures(Collection<? extends BooleanFormula> features) {
		HashSet<? extends BooleanFormula> featureSet = new HashSet<BooleanFormula>(
				features);
		// check old features to be still relevant, if so set flag to false, otherwise remove
		// return the features that are new.
		Iterator<Pair<Double, Pair<? extends BooleanFormula, Boolean>>> it = scores.iterator();
		while (it.hasNext()){
			Pair<Double, Pair<? extends BooleanFormula, Boolean>> score = it.next();
			BooleanFormula feature = score.right()
					.left();
			if (featureSet.contains(feature)) {
				featureSet.remove(feature);
				score.right().setRight(false);
			} else{
				it.remove();
			}
		}
		return featureSet;
	}

	private void init() {
		scorer.init();
	}


	public void print(int l) {
		System.out.println("Scores");
		int i = 0;
		for (Pair<Double, Pair<? extends BooleanFormula, Boolean>> score : scores) {
			i++;
			if (i > l) {
				break;
			}
			System.out.println(score);
		}
		System.out.println();
	}

}
