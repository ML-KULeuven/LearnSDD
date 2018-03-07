#include "weightlearner_jni.h"

JNIEXPORT void JNICALL Java_weightlearner_LbfgsWeightLearnerJNI_learnWeights
  (JNIEnv * env, jclass cls, jlong jsdd, jlong jmgr, jint jnbFeatures, jintArray jcounts, jint jnbInstances, jdoubleArray jweights){
    
    SddNode *csdd = *(SddNode **)&jsdd; 
    SddManager *cmgr = *(SddManager **)&jmgr;
    int cnbFeatures = jnbFeatures; 
    
    int *ccounts = (*env)->GetIntArrayElements(env, jcounts, NULL);
    if (NULL == ccounts) return;
    
    int cnbInstances = jnbInstances;
    
    double *cweights = (*env)->GetDoubleArrayElements(env, jweights, NULL);
    if (NULL == cweights) return;
    
    learnWeights(csdd, cmgr, cnbFeatures, ccounts, cnbInstances, cweights);
    
    
   (*env)->ReleaseIntArrayElements(env, jcounts, ccounts, 0);
   (*env)->ReleaseDoubleArrayElements(env, jweights, cweights, 0);
   
  }
  
JNIEXPORT void JNICALL Java_weightlearner_LbfgsWeightLearnerJNI_learnWeightsL1
  (JNIEnv * env, jclass cls, jlong jsdd, jlong jmgr, jint jnbFeatures, jintArray jcounts, jint jnbInstances, jdoubleArray jweights, jdouble jl1Const){
    
    SddNode *csdd = *(SddNode **)&jsdd; 
    SddManager *cmgr = *(SddManager **)&jmgr;
    int cnbFeatures = jnbFeatures; 
    
    int *ccounts = (*env)->GetIntArrayElements(env, jcounts, NULL);
    if (NULL == ccounts) return;
    
    int cnbInstances = jnbInstances;
    
    double *cweights = (*env)->GetDoubleArrayElements(env, jweights, NULL);
    if (NULL == cweights) return;
    
    double l1Const = jl1Const;
    
    learnWeights_l1(csdd, cmgr, cnbFeatures, ccounts, cnbInstances, cweights, l1Const);
    
    
   (*env)->ReleaseIntArrayElements(env, jcounts, ccounts, 0);
   (*env)->ReleaseDoubleArrayElements(env, jweights, cweights, 0);
   
  }
