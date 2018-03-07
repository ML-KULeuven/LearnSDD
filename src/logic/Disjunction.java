package logic;

import data.Instance;
import utilities.ArrayUtils;

import java.util.*;


/**
 * Represents a disjunction
 */
public class Disjunction extends BooleanFormula {
	
	public Disjunction(Collection<Integer> elements) {
		this.elements = new ArrayList<Integer>(elements);
	}


	public Disjunction(int... elements) {
		this.elements = ArrayUtils.arrayIntegerToList(elements);
	}

	ArrayList<Integer> elements;
	
	@Override
	public boolean presentIn(Instance instance) {
		for (int e: elements){
			if (e>0){
				if(instance.get(e))
					return true;
			}
			else {
				if(!instance.get(-e))
					return true;
			}
		}
		return false;
	}

	@Override
	public SddFormula toTerm() {
		SddFormula res = new SddFormula(false);
		for (int el : elements) {
			res = res.disjoin(new SddFormula(el));
		}
		return res;
	}

	@Override
	public boolean isLiteral() {
		if (elements.size() == 1)
			return true;
		return false;
	}
	
	public int head() {
		return elements.get(0);
	}

	public List<Integer> tail() {
		return elements.subList(1, elements.size());
	}
	
	@Override
	public int size() {
		return elements.size();
	}

	public void addAll(List<Integer> tail) {
		elements.addAll(tail);
	}

	public void add(int v) {
		elements.add(v);
	}
	
	@Override
	public String getName() {
		return "disj";
	}

	@Override
	public boolean equals(Object o) {
		if (o==null || ! (o instanceof Disjunction))
			return false;
		Disjunction other = (Disjunction) o;
		if (elements.size() != other.elements.size()) {
			return false;
		}
		return other.elements.containsAll(elements);
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		for(int element: elements)
			hashCode+=element; 
		return hashCode;
	}
	
	@Override
	public BooleanFormula convertToTermRepresentation(String string) {
		String[] elementStrings = string.split(" ");
		int nbElements = elementStrings.length;
		int[] elements = new int[nbElements];
		for(int i=0; i<nbElements; i++)
			elements[i]=Integer.parseInt(elementStrings[i]);
		return new Disjunction(elements);
	}

	@Override
	public String convertToString() {
		String str = "";
		if (elements.size()>0){
			for (int el : elements){
				str += el+" ";
			}
			str = str.substring(0, str.length()-1);
		}
		return str;
	}


	public Set<Integer> getElements() {
		return new HashSet<Integer>(elements);
	}
	
	@Override
	public String toString() {
		String res = "disj [";
		for (int el : elements) {
			res += el + ",";
		}
		res = res.substring(0, res.length() - 1) + "]";
		return res;
	}
	
	@Override
	public boolean containsVar(int f) {
		return elements.contains(f) || elements.contains(-f);
	}


	@Override
	public long[] getParents() {
		long[] parents = new long[elements.size()];
		for(int i=0; i<parents.length; i++)
			parents[i]=Math.abs(elements.get(i));
		return parents;
	}
}
