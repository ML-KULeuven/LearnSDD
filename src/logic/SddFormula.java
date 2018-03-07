package logic;

import sdd.Sdd;
import sdd.SddManager;

/**
 * This class simplifies working with sdds by doing the (de)referencing automatically and always assuming the same manager.
 */
public class SddFormula {
	
	private Sdd sdd;
	private boolean isLiteral;
	private boolean isTrue;
	private boolean isFalse;

    private SddFormula(){}


	public SddFormula(boolean val){
		isLiteral = false;
		isTrue = val;
		isFalse = !val;
		sdd = new Sdd(val, manager);
	}
	
	public SddFormula(int literal){
		if(literal==0){
			isLiteral = false;
			isTrue = true;
			isFalse = false;
			sdd = new Sdd(true, manager);
		}
		else {
			isLiteral = true;
			isTrue = false;
			isFalse = false;
			if (manager.getVarCount()< literal)
				manager.addVarAfterLast();
			sdd = new Sdd(literal, manager);
		}
	}
	
	public SddFormula(Sdd sdd) {
		this.sdd = sdd;
		this.isLiteral = sdd.isLiteral();
		this.isTrue = sdd.isTrue();
		this.isFalse = sdd.isFalse();
	}

	private static SddManager manager;
	
	public static void setManager(SddManager manager){
		SddFormula.manager = manager;
	}

	public boolean isLiteral() {
		return isLiteral;
	}

	public boolean isBoolean() {
		return isTrue || isFalse;
	}
	
	public void ref(){
		sdd.ref();
	}
	
	public void deref(){
		sdd.deref();
	}
	
	public SddFormula negate() {
		SddFormula res = new SddFormula();
		res.sdd = this.sdd.negate();
		res.isLiteral = this.isLiteral;
		res.isTrue = this.isFalse;
		res.isFalse = this.isTrue;
		return res;
	}

	public SddFormula conjoin(SddFormula other) {
		SddFormula res = new SddFormula();
		res.sdd = this.sdd.conjoin(other.sdd);
		res.isLiteral = (this.isTrue || other.isTrue) && (this.isLiteral||other.isLiteral);
		res.isTrue = (this.isTrue && other.isTrue);
		res.isFalse = (this.isFalse || other.isFalse);
		return res;
	}

	public SddFormula disjoin(SddFormula other) {
		SddFormula res = new SddFormula();
		res.sdd = this.sdd.disjoin(other.sdd);
		res.isLiteral = (this.isFalse || other.isFalse) && (this.isLiteral||other.isLiteral);
		res.isTrue = (this.isTrue || other.isTrue);
		res.isFalse = this.isFalse && other.isFalse;
		return res;
	}
	
	public SddFormula imply(SddFormula other) {
		this.ref();other.ref();
		SddFormula neg = this.negate();
		SddFormula implication = neg.disjoin(other);
		this.deref(); other.deref();
		return implication;
	}

	public SddFormula equiv(SddFormula other) {
		this.ref();other.ref();
		SddFormula imp1 = this.imply(other);
		imp1.ref();
		SddFormula imp2 = other.imply(this);
		SddFormula equivalence = imp1.conjoin(imp2);
		this.deref(); other.deref(); imp1.deref();
		return equivalence;
	}
	
	public SddFormula condition(int literal){
		SddFormula conditioned = new SddFormula(this.sdd.condition(literal));
		return conditioned;
	}

	public Sdd getSdd() {
		return sdd;
	}


	public String toString() {
		if (isFalse)
			return "false";
		if (isTrue)
			return "true";
		if (isLiteral)
			return ""+sdd.getLiteral();
		return "decisionNode";
		
	}
}
