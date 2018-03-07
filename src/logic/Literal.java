package logic;

import data.Instance;

/**
 * Represents a literal
 */
public class Literal extends BooleanFormula {

	int lit; 
	public Literal(int lit) {
		this.lit = lit;
	}
	
	@Override
	public boolean presentIn(Instance instance) {
		return instance.get(lit);
	}

	@Override
	public SddFormula toTerm() {
		return new SddFormula(lit);
	}

	@Override
	public boolean isLiteral() {
		return true;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public String getName() {
		return "lit";
	}

	@Override
	public Literal convertToTermRepresentation(String string) {
		return new Literal(Integer.parseInt(string));
	}

	@Override
	public String convertToString() {
		return Integer.toString(lit);
	}
	
	@Override
	public boolean containsVar(int f) {
		return lit==f || lit==-f;
	}

	@Override
	public long[] getParents() {
		return new long[]{Math.abs(lit)};
	}

}
