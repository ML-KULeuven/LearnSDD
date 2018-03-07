package logic;

import data.Instance;

/**
 * Represents a logical formula
 */
public abstract class BooleanFormula {
	
	public abstract boolean presentIn(Instance instance);
	
	public abstract SddFormula toTerm();

	public abstract boolean isLiteral();
	
	public abstract int size();
	
	public boolean makesRedundant(BooleanFormula o){
		return false;
	}

	public abstract  String getName();
	
	public abstract BooleanFormula convertToTermRepresentation(String string);
	
	public abstract String convertToString();

	public abstract boolean containsVar(int f);

	public abstract long[] getParents();
	
	protected double probabilityInData = -1;
	protected double lastKLD = -1;
	
	public void setProbabilityInData(double probabilityInData) {
		this.probabilityInData=probabilityInData;
	}
	
	public double getProbabilityInData() {
		return probabilityInData;
	}

	public void setLastKLD(double lastKLD) {
		this.lastKLD=lastKLD;
	}
	
	public double getLastKLD() {
		return lastKLD;
	}


	
}
