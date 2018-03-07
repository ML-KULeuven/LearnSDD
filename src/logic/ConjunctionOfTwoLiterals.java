package logic;

import utilities.Pair;

import java.util.List;

/**
 * Represents a conjunction of 2 literals
 */
public class ConjunctionOfTwoLiterals extends ConjunctionOfLiterals {
	
	private Pair<Integer, Integer> p;
	
	public ConjunctionOfTwoLiterals(Pair<Integer, Integer> p) {
		super(p.left(), p.right());
		this.p = p;
	}
	
	public ConjunctionOfTwoLiterals(int l, int r) {
		this(new Pair<Integer, Integer>(l, r));
	}
	
	public Pair<Integer, Integer> getConjunctionPair() {
		return p;
	}

	public String toParseString() {
		return p.left() +" "+ p.right();
	}
	
	public static ConjunctionOfTwoLiterals parse(String parseString){
		String[] parts = parseString.split(" ");
		int l = Integer.parseInt(parts[0]);
		int r = Integer.parseInt(parts[1]);
		return new ConjunctionOfTwoLiterals(l,r);
	}
	
	@Override
	public void addAll(List<Integer> tail) {
		if (tail.size()>0)
			throw new IllegalArgumentException("Cannot add elements to binary conjunction");
	}
	
	@Override
	public String getName() {
		return "binconj";
	}

	@Override
	public ConjunctionOfLiterals convertToTermRepresentation(String string) {
		String[] elementStrings = string.split(" ");
		int nbElements = elementStrings.length;
		if (nbElements!=2)
			throw new IllegalArgumentException(string + " is not a valid encoding for a binary conjunction representation");
		int[] elements = new int[nbElements];
		for(int i=0; i<nbElements; i++)
			elements[i]=Integer.parseInt(elementStrings[i]);
		return new ConjunctionOfTwoLiterals(elements[0], elements[1]);
	}
}
