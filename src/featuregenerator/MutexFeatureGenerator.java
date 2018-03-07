package featuregenerator;

import learnsdd.Model;
import data.Data;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.graph.Pseudograph;
import logic.*;
import utilities.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Generate mutex and features.
 * Possible features are mutex and multival features.
 * a mutex feature over a set of variables {a,b,c} dictate that at most 1 of a,b and c are positive at the same time.
 * a multival feature over a set of variables {a,b,c} dictate that exactly 1 of a,b and c is positive at the same time.
 *
 * The features are generated once, the first time the method is called.
 * To find mutually exclusive sets of variables, the Bron-Kerbosch clique detection algorithm is used, implemented in jgrapht
 */
public class MutexFeatureGenerator implements FeatureGenerator {

	private double threshold;

	public MutexFeatureGenerator(double threshold) {
		this.threshold = threshold;
	}

	private ArrayList<Conjunction> possibilities;

	private boolean first = true;
    private boolean firstWasThisRound=true;

	@Override
	public List<Conjunction> generate(
			Model model) {
		if (first) {
			initialize(model.getTrainingData());
			first = false;
		}
        else {
            firstWasThisRound=false;
        }

		return new ArrayList<Conjunction>(possibilities);
	}

	private void initialize(Data data) {

		Graph<Integer, Pair<Integer, Integer>> graph = new Pseudograph<Integer, Pair<Integer, Integer>>(
				new EdgeFactory<Integer, Pair<Integer, Integer>>() {

					@Override
					public Pair<Integer, Integer> createEdge(
							Integer sourceVertex, Integer targetVertex) {
						return new Pair<Integer, Integer>(sourceVertex,
								targetVertex);
					}
				});

		addVerticesToGraph(graph, data);
		addEdgesToGraph(graph, data);
		BronKerboschCliqueFinder<Integer, Pair<Integer, Integer>> cliqueFinder = new BronKerboschCliqueFinder<Integer, Pair<Integer, Integer>>(
				graph);
		Collection<Set<Integer>> maximalCliques = cliqueFinder
				.getAllMaximalCliques();

		possibilities = new ArrayList<Conjunction>();
		for (Set<Integer> clique : maximalCliques) {
			if (clique.size() < 2)
				continue;
			if (isMultivaluedVar(clique, data)) {
				possibilities.add(new ExactOne(clique));
			} else {
				possibilities.add(new Mutex(clique));
			}
		}
	}

	private void addVerticesToGraph(
			Graph<Integer, Pair<Integer, Integer>> graph, Data data) {
		for (int i = 1; i <= data.getNbVars(); i++)
			graph.addVertex(i);
	}

	private void addEdgesToGraph(Graph<Integer, Pair<Integer, Integer>> graph,
			Data data) {
		int nbInstances = data.getNbInstances();
		for (int i = 1; i <= data.getNbVars(); i++) {
			if (data.getCount(i) == 0)
				continue;
			for (int j = i + 1; j <= data.getNbVars(); j++) {
				if (data.getCount(j) == 0)
					continue;
				int count = data.getCount(new Disjunction(-i,
						-j));
				if (count / nbInstances >= threshold) {
					graph.addEdge(i, j);
				}
			}
		}
	}

	private boolean isMultivaluedVar(Set<Integer> clique, Data data) {
		Disjunction term = new Disjunction(
				clique);
		return data.getCount(term)/data.getNbInstances()>=threshold;
	}


	@Override
	public void informChosenFeature(BooleanFormula chosenFeatureCol) {
        if (chosenFeatureCol instanceof Conjunction) {
			possibilities.remove(chosenFeatureCol);
		}

	}

    @Override
    public List<Conjunction> getNewFeatures() {
        if (firstWasThisRound){
            return possibilities;
        }
        return new ArrayList<Conjunction>();
    }

    @Override
    public List<Conjunction> getDiscardedFeatures() {
        return new ArrayList<Conjunction>();
    }

	@Override
	public String toString(){
		return "mutex";
	}
}
