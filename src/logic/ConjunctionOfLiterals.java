package logic;

import data.Instance;

import java.util.*;


/**
 * Represents a conjunction of literals
 */
public class ConjunctionOfLiterals extends BooleanFormula {

	ArrayList<Integer> elements;


    public ConjunctionOfLiterals(int... elements) {
		this.elements = new ArrayList<Integer>();
		for (int el : elements)
			this.elements.add(el);
	}

	public ConjunctionOfLiterals(Collection<Integer> elements) {
		this.elements = new ArrayList<Integer>(elements);
	}

	public Set<Integer> getElements() {
		return new HashSet<Integer>(elements);
	}



	@Override
	public boolean presentIn(Instance instance) {
		for (int el : elements) {
			if (el > 0) {
				if (!instance.get(el))
					return false;
			} else {
				if (instance.get(-el))
					return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		String res = "conj[";
		for (int el : elements) {
			res += el + ",";
		}
		res = res.substring(0, res.length() - 1) + "]";
		return res;
	}

	@Override
	public SddFormula toTerm() {
		SddFormula res = new SddFormula(true);
		for (int el : elements) {
			res = res.conjoin(new SddFormula(el));
		}
//		res.setRepr(this);
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

	@Override
	public boolean equals(Object o) {
		if (o==null || ! (o instanceof ConjunctionOfLiterals))
			return false;
		ConjunctionOfLiterals other = (ConjunctionOfLiterals) o;
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
	public String getName() {
		return "conj";
	}

	@Override
	public ConjunctionOfLiterals convertToTermRepresentation(String string) {
		String[] elementStrings = string.split(" ");
		int nbElements = elementStrings.length;
		int[] elements = new int[nbElements];
		for(int i=0; i<nbElements; i++)
			elements[i]=Integer.parseInt(elementStrings[i]);
		return new ConjunctionOfLiterals(elements);
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

	public void add(int element) {
		elements.add(element);
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
