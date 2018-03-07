package featurefilter;

import learnsdd.Model;
import logic.BooleanFormula;

import java.util.Collections;
import java.util.List;

/**
 * Random subset filtering.
 */
public class RandomFilter implements FeatureFilter {

    private final int limit;

    public RandomFilter(int limit){
        this.limit = limit;
    }

    @Override
    public List<? extends BooleanFormula> filter(List<? extends BooleanFormula> candidates, Model model) {
        if (candidates.size()<=limit){
            return candidates;
        } else {
            Collections.shuffle(candidates);
            return candidates.subList(0,limit);
        }
    }
}
