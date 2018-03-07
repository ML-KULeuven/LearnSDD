package utilities;

/**
 * Implements a pair: a tuple with two values
 * @param <L>
 * @param <R>
 */
public class Pair<L,R> {

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}
	
	private L left;
	private R right;
	
	public L left() {
		return left;
	}
	
	public R right() {
		return right;
	}
	
	@Override
	public String toString(){
		return "< "+left + " ; " + right+ " >";
	}
	
	@Override
	public boolean equals(Object o) {
		return o!=null && (o instanceof Pair<?,?>) && left().equals(((Pair<?, ?>) o).left()) && right().equals(((Pair<?, ?>) o).right()); 		
	}
	
	@Override
	public int hashCode(){
		return left.hashCode()+right.hashCode();
	}

	public void setRight(R right) {
		this.right=right;
	}
	
	public void setLeft(L left) {
		this.left=left;
	}
	
}
