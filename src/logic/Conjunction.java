package logic;

import data.Instance;

import java.util.ArrayList;

/**
 * Represents a conjunction of boolean formulas
 */
public class Conjunction extends BooleanFormula {

	ArrayList<BooleanFormula> elements;
	
	public Conjunction(ArrayList<BooleanFormula> elements) {
		this.elements=elements;
	}
	
	@Override
	public boolean presentIn(Instance instance) {
		for (BooleanFormula el : elements)
			if (!el.presentIn(instance))
				return false;
		return true;
	}

	@Override
	public SddFormula toTerm() {
		SddFormula res = new SddFormula(true);
		for (BooleanFormula el : elements) {
			res.ref();
			SddFormula sddFormula =el.toTerm();
			res.deref();
			res = res.conjoin(sddFormula);
		}
		return res;
	}

	@Override
	public boolean isLiteral() {
		if (elements.size() == 1)
			return elements.get(0).isLiteral();
		return false;
	}
	
	@Override
	public boolean containsVar(int f) {

		for (BooleanFormula el : elements){
			if (el.containsVar(f))
				return true;
		}
		return false;
	}
	
	int size=-1;

	@Override
	public int size() {
		if (size!=-1)
			return size;
		size = 0;
		for (BooleanFormula el: elements)
			size += el.size();
		return size;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o==null || ! (o instanceof Conjunction))
			return false;
		Conjunction other = (Conjunction) o;
		if (elements.size() != other.elements.size()) {
			return false;
		}
		return other.elements.containsAll(elements);
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		for(BooleanFormula element: elements)
			hashCode+=element.hashCode(); 
		return hashCode;
	}

	@Override
	public String getName() {
		return "termConj";
	}

	@Override
	public BooleanFormula convertToTermRepresentation(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String convertToString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long[] getParents() {
		int length = 0;
		for(BooleanFormula element: elements)
			length+=element.getParents().length;
		long[] parents = new long[length];
		int i=0;
		for(BooleanFormula element: elements){
			for(long parent: element.getParents()){
				parents[i]=parent;
				i++;
			}
		}
			
		return parents;
	}

}
