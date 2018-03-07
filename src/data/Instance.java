package data;

import logic.BooleanFormula;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Represents an instance of the data.
 */
public class Instance implements Iterable<Boolean> {
	
	@SuppressWarnings("unchecked")
	private Instance(Instance instance){
		this.featurePresent = (ArrayList<Boolean>) instance.featurePresent.clone();
		this.weight = instance.weight;
	}
	
	public Instance(int weight, boolean...featurePresences){
		this.weight = weight;
		this.featurePresent = new ArrayList<Boolean>();
		featurePresent.add(false);// feature 0 does not exist
		for (boolean f: featurePresences)
			featurePresent.add(f);
	}

	public Instance(String dataline, ArrayList<Double> counts) {
		if (dataline.isEmpty())
			throw new IllegalArgumentException("empty line");

		featurePresent = new ArrayList<Boolean>();
		featurePresent.add(false); // feature 0 does not exist
		
		String[] parts = dataline.split("\\|");

		if (parts.length==1)
			weight = 1;
		else {
			weight = Integer.parseInt(parts[0]);
			dataline = parts[1];
		}

		if (counts.isEmpty()) {
			Scanner scanner = new Scanner(dataline);
			scanner.useDelimiter(",");
			counts.add(0.0); //the count of a nonexisting feature is 0
			while (scanner.hasNext()) {
				String next = scanner.next();
				if (next.equals("0")){
					featurePresent.add(false);
					counts.add(0.0);
				}
				else if (next.equals("1")) {
					featurePresent.add(true);
					counts.add(weight);
				} else {
					scanner.close();
					throw new IllegalArgumentException(
							"line has other symbols than '0', '1' and ','");
				}
			}
			scanner.close();

		} else {
			Scanner scanner = new Scanner(dataline);
			scanner.useDelimiter(",");
			int i = 1;
			while (scanner.hasNext()) {
				String next = scanner.next();
				if (next.equals("0"))
					featurePresent.add(false);
				else if (next.equals("1")) {
					featurePresent.add(true);
					counts.set(i, counts.get(i) + weight);
				} else {
					scanner.close();
					throw new IllegalArgumentException(
							"line has other symbols than '0', '1' and ','");
				}
				i++;
			}
			scanner.close();
		}
	}

	private ArrayList<Boolean> featurePresent;
	private double weight;

	public int getNbFeatures() {
		return featurePresent.size() - 1;
	}

	public double addFeature(BooleanFormula feature) {
		double count = checkWeight(feature);
		featurePresent.add(count>0);
		return count;
	}

	public double setFeature(int featureNb, BooleanFormula feature) {
		double count = checkWeight(feature);
		featurePresent.set(featureNb, count>0);
		return count;
	}

	public void print() {
		String str = weight + "|";
		for(boolean p : featurePresent)
			str += p? "1 " : "0 ";
		System.out.println(str);
	}

	@Override
	public Iterator<Boolean> iterator() {
		return featurePresent.iterator();
	}

	public Instance copy() {
		return new Instance(this);
	}

	public boolean get(int feature) {
		return featurePresent.get(feature);
	}
	
	public double getWeight() {
		return weight;
	}

	public double checkWeight(BooleanFormula term) {
		boolean present = term.presentIn(this);
		return present ? weight : 0;
	}

}
