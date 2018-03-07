package logic;

import java.util.ArrayList;
import java.util.Collection;

/**
 * represents an exact-1 relationship between variables.
 * This is the case for multivalued variables which were binarized: there is always 1 value true.
 */
public class ExactOne extends Conjunction {

	private ArrayList<Integer> multivalElements;

	public ExactOne(Collection<Integer> clique) {
		super(constructClauses(new ArrayList<Integer>(clique)));
		this.multivalElements =new ArrayList<Integer>(clique);
	}
	

	public ExactOne(int x, int y) {
		super(constructClauses(new ArrayList<Integer>(makeClique(x, y))));
		this.multivalElements =new ArrayList<Integer>(makeClique(x, y));
	}

	private static Collection<Integer> makeClique(int x, int y){
		Collection<Integer> clique = new ArrayList<Integer>();
		clique.add(x);
		clique.add(y);
		return clique;
	}
	
	private static ArrayList<BooleanFormula> constructClauses(
			ArrayList<Integer> elements) {
		ArrayList<BooleanFormula> clauses = new ArrayList<BooleanFormula>();
		
		//max 1
		for(int i=0; i<elements.size(); i++){
			for(int j=i+1; j<elements.size(); j++){
				clauses.add(new Disjunction(-elements.get(i), -elements.get(j)));
			}
		}
		
		//min 1
		clauses.add(new Disjunction(elements));
		
		return clauses;
	}
	
	@Override
	public BooleanFormula convertToTermRepresentation(String string) {
		String[] elementStrings = string.split(" ");
		int nbElements = elementStrings.length;
		ArrayList<Integer> elements = new ArrayList<Integer>(nbElements);
		for(int i=0; i<nbElements; i++)
			elements.add(Integer.parseInt(elementStrings[i]));
		return new ExactOne(elements);
	}

	@Override
	public String convertToString() {
		String str = "";
		if (elements.size()>0){
			for (int el : multivalElements){
				str += el+" ";
			}
			str = str.substring(0, str.length()-1);
		}
		return str;
	}
	
	@Override
	public String getName() {
		return "exact-1";
	}


	@Override
	public String toString() {
		return "exact-1: " + multivalElements.toString();
	}
}
