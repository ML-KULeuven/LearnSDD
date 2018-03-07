package learnsdd;

import data.Data;
import data.Instance;
import logic.BooleanFormula;
import logic.Literal;
import logic.SddFormula;
import sdd.SddManager;
import sdd.WmcManager;
import weightlearner.LbfgsWeightLearner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Markov Network model and its SDD.
 */
public class Model {

	private int nbVars;
	private ArrayList<BooleanFormula> features; //the formulas of the features
	private ArrayList<Double> weights; // the weight for each feature (in log space)
	private SddFormula theory; //The logical formula that is used for weighted model counting.
	private LbfgsWeightLearner weightLearner = new LbfgsWeightLearner();

	private Data trainData;
	private Data validData;

	private SddManager manager;

	private double partitionFunction; // The partition function of the Markov Network, which is the weighted model count of the theory.
	private double trainLogLikelihood;
	private double validLogLikelihood;
	private ArrayList<Double> probabilities;

	private int featuresSize = 0;

	public SddManager getManager() {
		return manager;
	}

	private Model(Model m) {
		this.nbVars = m.nbVars;
		this.features = new ArrayList<BooleanFormula>(m.features);
		this.weights = new ArrayList<Double>(m.weights);
		this.theory = m.theory;
		this.trainData = m.trainData;
		this.validData = m.validData;
		this.manager = m.manager;
		this.featuresSize = m.featuresSize;
	}

	public Model(Data trainData, Data validData, SddManager manager) {
		this.trainData = trainData;
		this.validData = validData;
		this.manager = manager;
		initialize();
	}

	private void initialize() {
		assert (trainData.getNbFeatures() == validData.getNbFeatures());
		assert (trainData.getNbFeatures() == trainData.getNbVars());
		nbVars = trainData.getNbVars();


		features = new ArrayList<BooleanFormula>();
		weights = new ArrayList<Double>();

		for (int var = 0; var <= nbVars; var++) {
			features.add(new Literal(var));
			weights.add(0.0);
		}
		theory = new SddFormula(true);
	}

	public int getNbOfVars() {
		return nbVars;
	}

	public int getNbOfFeatures() {
		return features.size() - 1;
	}

	public Data getTrainingData() {
		return trainData;
	}

	public Data getValidationData() {
		return validData;
	}

	public BooleanFormula getFeature(int f) {
		return features.get(f);
	}

	public double getProbability(int f) {
		return Math.exp(probabilities.get(f));
	}

	public double getWeight(int f) {
		return weights.get(f);
	}

	public ArrayList<Double> getWeights() {
		return weights;
	}

	public Model addFeature(BooleanFormula feature) {
		if (feature.isLiteral()) {
			return new Model(this);
		}
		this.ref();
		int featureNb = features.size();
		Model extendedModel = new Model(this);
		extendedModel.features.add(feature);
		extendedModel.weights.add(0.0);

		SddFormula featureSddFormula = feature.toTerm();
		featureSddFormula.ref();
		extendedModel.theory = this.theory.conjoin((new SddFormula(featureNb)
				.equiv(featureSddFormula)));
		featureSddFormula.deref();
		extendedModel.trainData = trainData.addFeature(feature);
		extendedModel.validData = validData.addFeature(feature);
		extendedModel.featuresSize += feature.size();

		this.deref();
		return extendedModel;
	}



	public void learnWeights() {
		weights = weightLearner.learn(this);
		updateWeightRelatedStuff();

	}

	public void setWeights(List<Double> weights) {
		assert (weights.size() == this.weights.size());
		this.weights = new ArrayList<Double>(weights);
		// update Z en loglikelihoods
		updateWeightRelatedStuff();

	}

	public void updateWeightRelatedStuff() {
		updatePartitionFunction();
		trainLogLikelihood = calculateLogLikelihood(trainData);
		validLogLikelihood = calculateLogLikelihood(validData);
	}

	private void updatePartitionFunction() {
		WmcManager wmc = new WmcManager(theory.getSdd(), true);
		for (int i = 1; i < weights.size(); i++) {
			wmc.setLiteralWeight(i, weights.get(i));
		}

		partitionFunction = wmc.propagate();
		probabilities = new ArrayList<Double>();
		probabilities.add(0.0);
		for (int f = 1; f <= getNbOfFeatures(); f++)
			probabilities.add(wmc.getProbability(f));
		wmc.free();
	}
	
	public double getWMC() {
		return partitionFunction;
	}

	/**
	 * 
	 * precondition: Data should have all features (This is the case for
	 * traindata and validdata) precondition: learnweights should be called
	 * after addFeature before using this.
	 * 
	 * @param data
	 * @return
	 */
	private double calculateLogLikelihood(Data data) {
		if (partitionFunction == 0)
			throw new RuntimeException(
					"Partition function was zero when trying to calculate likelihood");
		double LL = 0;
		for (int f = 0; f <= data.getNbFeatures(); f++) {
			double w = weights.get(f);
			if (w != Double.NEGATIVE_INFINITY)
				LL += ((double) data.getCount(f)) * weights.get(f);
		}
		LL = LL / data.getNbInstances() - partitionFunction;
		return LL;
	}

	public double getTrainLogLikelihood() {
		if (trainLogLikelihood == 0)
			throw new RuntimeException(
					"zero loglikelihood is impossible, perhabs it was not calculated yet");
		return trainLogLikelihood;
	}

	public double getValidLogLikelihood() {
		if (validLogLikelihood == 0)
			throw new RuntimeException(
					"zero loglikelihood is impossible, perhabs it was not calculated yet");
		return validLogLikelihood;
	}

	public void ref() {
		theory.ref();
	}

	public void deref() {
		theory.deref();
	}

	public long getSize() {
		return theory.getSdd().getSize();
	}
	
	public double getProbability(Instance instance){
		
		WmcManager wmcManager = new WmcManager(theory.getSdd(), true);
		for (int v = 1; v <= getNbOfVars(); v++) {
			wmcManager.setLiteralWeight(instance.get(v) ? -v : v,
					wmcManager.getZeroWeight());
			wmcManager.setLiteralWeight(instance.get(v) ? v : -v,
					wmcManager.getOneWeight());
		}
		double logprop = wmcManager.propagate() - partitionFunction;
		return Math.exp(logprop);

	}

	public double getLogLikelihood(Data data) {
		if (data.getNbVars() != getNbOfVars())
			throw new IllegalArgumentException(
					"The data should have the same nb of vars as the model");

		// calculate weighted model count of model
		WmcManager wmcManager = new WmcManager(theory.getSdd(), true);
		for (int f = 1; f < weights.size(); f++) {
			wmcManager.setLiteralWeight(f, weights.get(f));
		}
		double wmc = wmcManager.propagate();

		// calculate distribution of data= 1/N*sum_{each feature f}(count in
		// data)
		double count = 0;
		for (Instance instance : data) {
			for (int v = 1; v <= data.getNbVars(); v++) {
				wmcManager.setLiteralWeight(instance.get(v) ? -v : v,
						wmcManager.getZeroWeight());
				wmcManager.setLiteralWeight(instance.get(v) ? v : -v,
						wmcManager.getOneWeight());
			}
			wmcManager.propagate();
			for (int f = 1; f < weights.size(); f++) {
				if (wmcManager.getProbability(f) > -1000)
					count += instance.getWeight() * weights.get(f);
			}
		}
		wmcManager.free();
		double LL = count / ((double) data.getNbInstances()) - wmc;
		return LL;
	}

	public SddFormula getTheory() {
		return theory;
	}


	public int getFeaturesSize() {
		return featuresSize;
	}

	public void saveWeights(File file) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(file);
		for (double weight :weights){
			writer.println(weight);
		}
		writer.close();
	}

    public void saveFeatures(File file) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(file);
        for (int i=0; i<features.size(); i++){
            writer.println(i+": "+features.get(i).toString());
        }
        writer.close();
    }
}
