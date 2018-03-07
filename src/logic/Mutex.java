package logic;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a mutually exclusive relationship between variables: at most 1 variable is true.
 */
public class Mutex extends Conjunction {
	
	private ArrayList<Integer> mutexElements;

	public Mutex(Collection<Integer> elements) {
		super(constructClauses(new ArrayList<Integer>(elements)));
		this.mutexElements =new ArrayList<Integer>(elements);
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
		
		return clauses;
	}
	

	@Override
	public BooleanFormula convertToTermRepresentation(String string) {
		String[] elementStrings = string.split(" ");
		int nbElements = elementStrings.length;
		ArrayList<Integer> elements = new ArrayList<Integer>(nbElements);
		for(int i=0; i<nbElements; i++)
			elements.add(Integer.parseInt(elementStrings[i]));
		return new Mutex(elements);
	}

	@Override
	public String convertToString() {
		String str = "";
		if (elements.size()>0){
			for (int el : mutexElements){
				str += el+" ";
			}
			str = str.substring(0, str.length()-1);
		}
		return str;
	}
	
	@Override
	public String toString() {
		return "mutex: "+ mutexElements.toString();
	}
	
	@Override
	public String getName() {
		return "mutex";
	}

}
