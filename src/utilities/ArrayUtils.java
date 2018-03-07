package utilities;

import java.util.ArrayList;

/**
 * Conversions between arrays and arraylists
 */
public class ArrayUtils {
	
	public static double[] listDoubleToArray(ArrayList<Double> list){
		double[]array = new double[list.size()];
		for(int i=0; i<list.size(); i++)
			array[i] = list.get(i);
		return array;
	}

	public static int[] listIntegerToArray(ArrayList<Integer> list){
		int[]array = new int[list.size()];
		for(int i=0; i<list.size(); i++)
			array[i] = list.get(i);
		return array;
	}
	

	public static int[] doubleListIntegerToArray(ArrayList<Double> list){
		int[]array = new int[list.size()];
		for(int i=0; i<list.size(); i++)
			array[i] = (int) Math.round((double) list.get(i));
		return array;
	}
	
	public static ArrayList<Double> arrayDoubleToList(double[] array){
		ArrayList<Double> list = new ArrayList<Double>();
		for(double el : array)
			list.add(el);
		return list;
	}

	public static ArrayList<Integer> arrayIntegerToList(
			int[] array) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int el : array)
			list.add(el);
		return list;
	}

}
