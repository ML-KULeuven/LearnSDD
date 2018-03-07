package weightlearner;

import java.io.File;

import swig.SWIGTYPE_p_sdd_manager_t;
import swig.SWIGTYPE_p_sdd_node_t;
import swig.SwigPointer;

public class LbfgsWeightLearnerJNI {

	private static native void learnWeights(long sdd, long mgr, int nbFeatures, int[] counts,
			int nbInstances, double[] weights);
	
	private static native void learnWeightsL1(long sdd, long mgr, int nbFeatures, int[] counts,
			int nbInstances, double[] weights, double l1Const);
	

	public static void learnWeights(SWIGTYPE_p_sdd_node_t sdd, SWIGTYPE_p_sdd_manager_t mgr, int nbFeatures, int[] counts,
			int nbInstances, double[] weights){
//		learnWeights(SwigPointer.getCPtr(sdd) , SwigPointer.getCPtr(mgr), nbFeatures, counts, nbInstances, weights);
		learnWeights(sdd, mgr, nbFeatures, counts, nbInstances, weights, 0.95);
	}
	
	public static void learnWeights(SWIGTYPE_p_sdd_node_t sdd, SWIGTYPE_p_sdd_manager_t mgr, int nbFeatures, int[] counts,
			int nbInstances, double[] weights, double l1Const){
		learnWeightsL1(SwigPointer.getCPtr(sdd) , SwigPointer.getCPtr(mgr), nbFeatures, counts, nbInstances, weights, l1Const);
	}
	
	static {
		System.out.println(new File(System.getProperty("java.library.path")).getAbsolutePath());
		System.loadLibrary("weightlearner");
	}

}
