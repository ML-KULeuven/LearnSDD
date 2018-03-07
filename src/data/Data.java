package data;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import logic.BooleanFormula;
import utilities.ArrayUtils;

/**
 * Immutable class that holds a matrix of samples and features.
 * 
 * @author jessa
 *
 */
public class Data implements Iterable<Instance>{

	private ArrayList<Instance> instances;
	private ArrayList<Double> counts;
	private double weightSum;
	private int nbVars;
	private String name;
	
	public Data(ArrayList<Instance> instances, ArrayList<Double> counts,
			int nbVars, double weightSum) {
		this.instances = instances;
		this.counts = counts;
		this.nbVars = nbVars;
		this.weightSum=weightSum;
	}

	public Data(String path) {
		counts = new ArrayList<Double>();
		try {
			FileInputStream fstream = new FileInputStream(path);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			boolean first = true;
			while (first && (strLine = br.readLine()) != null) {
				if (readFirstInstance(strLine))
					first = false;
			}

			while ((strLine = br.readLine()) != null) {
				readInstance(strLine);
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		weightSum = 0;
		for(Instance instance : instances){
			weightSum+=instance.getWeight();
		}
		
	}

	public Data(String path, String dataName) {
		this(path);
		setName(dataName);
	}

	private boolean readFirstInstance(String line) {
		Instance instance;
		try {
			instance = new Instance(line, counts);

		} catch (IllegalArgumentException e) {
			return false;
		}
		instances = new ArrayList<Instance>();
		instances.add(instance);

		nbVars = instance.getNbFeatures();

		return true;
	}

	private void readInstance(String line) {
		Instance instance;
		try {
			instance = new Instance(line, counts);
		} catch (Exception e) {
			return;
		}
		instances.add(instance);

		if (nbVars != instance.getNbFeatures())
			throw new IllegalArgumentException(
					"Not all instances have the same number of variables!");

	}

	public int getNbVars() {
		return nbVars;
	}

	public int getNbFeatures() {
		return instances.get(0).getNbFeatures();
	}

	public Data addFeature(BooleanFormula feature) {
		double count = 0;
		ArrayList<Instance> extendedInstances = new ArrayList<Instance>();
		ArrayList<Double> extendedCounts = new ArrayList<Double>(counts);
		for (Instance instance : instances) {
			Instance extendedInstance = instance.copy();
			count += extendedInstance.addFeature(feature);
			extendedInstances.add(extendedInstance);
		}
		extendedCounts.add(count);
		return new Data(extendedInstances, extendedCounts, nbVars, weightSum);
	}

	
	public int getCount(BooleanFormula term) {
		int count = 0;
		for (Instance instance : instances) {
			count += instance.checkWeight(term);
		}
		return count;
	}

	public double getCount(int f) {
		return counts.get(f);
	}

	public int getNbInstances() {
		return (int) Math.round(weightSum);
	}
	
	public double getWeightSum(){
		return weightSum;
	}

	public double[] getWeights() {
		return ArrayUtils.listDoubleToArray(counts);
	}
	

	public int[] getCounts() {
		return ArrayUtils.doubleListIntegerToArray(counts);
	}

	@Override
	public Iterator<Instance> iterator() {
		return instances.iterator();
	}
	
	public void setName(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}



	public List<Instance> getInstances() {
		return new ArrayList<Instance>(instances);
	}


}