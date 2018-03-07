package featuregenerator;

import learnsdd.Model;
import logic.ConjunctionOfTwoLiterals;
import logic.ConjunctionOfLiterals;
import logic.BooleanFormula;
import utilities.Pair;

import java.util.*;

/**
 * Generate conjunctive features.
 * Possible features are (f1 and f2), (not f1 and f2),  (f1 and not f2), (not f1 and not f2),
 * where f1 and f2 are existing features in the model.
 *
 * The features are generated incrementally
 *
 */
public class ConjunctiveFeatureGenerator implements FeatureGenerator {


    boolean first = true;
    private List<ConjunctionOfTwoLiterals> possibilities;
    private List<ConjunctionOfTwoLiterals> newFeatures;
    private List<ConjunctionOfTwoLiterals> redundantFeatures;
    private BooleanFormula lastAdded;

    /**
     * Generate features: remove redundant, add new features.
     * @param model
     * @return
     */
    @Override
    public List<ConjunctionOfTwoLiterals> generate(Model model) {
        redundantFeatures = new ArrayList<ConjunctionOfTwoLiterals>();
        newFeatures = new ArrayList<ConjunctionOfTwoLiterals>();

        if (first) {
            possibilities = new ArrayList<ConjunctionOfTwoLiterals>();
            newFeatures = new ArrayList<ConjunctionOfTwoLiterals>(
                    generateInitialFeatures(model));
            first = false;
        }
        else{
            newFeatures = new ArrayList<ConjunctionOfTwoLiterals>();
            redundantFeatures.addAll(filterRedundant(possibilities, lastAdded));
            int featureNb = model.getNbOfFeatures();
            newFeatures.addAll(generateExtraFeatures(model, featureNb));

        }
        possibilities.addAll(newFeatures);
        return new ArrayList<ConjunctionOfTwoLiterals>(possibilities);
    }

    /**
     * Remove a feature that is already in the model from the possible new features.
     * @param feature
     */
    @Override
    public void informChosenFeature(
            BooleanFormula feature) {
        if (possibilities!=null && feature instanceof ConjunctionOfTwoLiterals)
            possibilities.remove(feature);
        lastAdded = feature;
    }



    private List<ConjunctionOfTwoLiterals> generateInitialFeatures(
            Model model) {

        completeDependencies(model);
        ArrayList<ConjunctionOfTwoLiterals> possibilities = new ArrayList<ConjunctionOfTwoLiterals>();
        for (int i = 2; i <= model.getNbOfFeatures(); i++) {
            for (int j = 1; j < i; j++) {
                addPossibilities(possibilities, j, i);
            }
        }

        return possibilities;
    }


    private List<ConjunctionOfTwoLiterals> generateExtraFeatures(
            Model model, int featureNb) {

        completeDependencies(model);

        ArrayList<ConjunctionOfTwoLiterals> possibilities = new ArrayList<ConjunctionOfTwoLiterals>();

        for (int var = 1; var <= model.getNbOfFeatures(); var++) {
            addPossibilities(possibilities,var, featureNb);
        }

        return possibilities;
    }

    // on which original variables the features depend (if f1=a, and f2=(a and b), then (f1 and f2) would be a non informative feature.)
    private ArrayList<HashSet<Integer>> dependencies = new ArrayList<HashSet<Integer>>();

    private void addPossibilities(ArrayList<ConjunctionOfTwoLiterals> possibilities, int var, int feature) {
        if (var == feature || dependencies.get(feature).contains(var))
            return;
        possibilities
                .add(new ConjunctionOfTwoLiterals(
                                new Pair<Integer, Integer>(feature, var)));
            possibilities
                    .add(new ConjunctionOfTwoLiterals(
                                    new Pair<Integer, Integer>(-feature, var)));
            possibilities
                    .add(new ConjunctionOfTwoLiterals(
                                    new Pair<Integer, Integer>(feature, -var)));
            possibilities
                    .add(new ConjunctionOfTwoLiterals(
                                    new Pair<Integer, Integer>(-feature, -var)));
    }


    private List<ConjunctionOfTwoLiterals> filterRedundant(
            List<ConjunctionOfTwoLiterals> possibilities,
            BooleanFormula feature) {

        List<ConjunctionOfTwoLiterals> redundantFeatures = new ArrayList<ConjunctionOfTwoLiterals>();

        if (feature instanceof ConjunctionOfLiterals){
            ConjunctionOfLiterals t = (ConjunctionOfLiterals) feature;
            Iterator<ConjunctionOfTwoLiterals> iterator = possibilities
                    .iterator();
            while (iterator.hasNext()) {
                ConjunctionOfTwoLiterals f = iterator.next();
                if (isRedundant(t.getElements(), f)){
                    redundantFeatures.add(f);
                    iterator.remove();
                }
            }
        }

        return redundantFeatures;
    }

    private boolean isRedundant(Set<Integer> tElements,
                                ConjunctionOfLiterals pos) {
        int oneNeg = tElements.size()==2?2:1;
        for (int el : pos.getElements()) {
            if (!tElements.contains(el)) {
                if (oneNeg>0 && tElements.contains(-el)) {
                    oneNeg--;
                }
                else
                    return false;
            }
        }
        return true;
    }

    private void completeDependencies(Model model) {
        while (dependencies.size() < model.getNbOfVars() + 1) {
            dependencies.add(new HashSet<Integer>());
        }
        while (dependencies.size() < model.getNbOfFeatures() + 1) {
            HashSet<Integer> newDependencies = new HashSet<Integer>();
            if (model.getFeature(dependencies.size()) instanceof ConjunctionOfLiterals) {
                for (int a : ((ConjunctionOfLiterals) model
                        .getFeature(dependencies.size())).getElements()) {
                    newDependencies.add(Math.abs(a));
                    newDependencies.addAll(dependencies.get(Math.abs(a)));
                }
            }
            dependencies.add(newDependencies);
        }
    }

    @Override
    public List<ConjunctionOfTwoLiterals> getNewFeatures() {

        return newFeatures;
    }

    @Override
    public List<ConjunctionOfTwoLiterals> getDiscardedFeatures() {
        return redundantFeatures;
    }

    @Override
    public String toString(){
        return "conj";
    }
}
