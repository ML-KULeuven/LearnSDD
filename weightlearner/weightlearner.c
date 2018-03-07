/*
 * weightLearnerc.c
 *
 *  Created on: Sep 30, 2014
 *      Author: jessa
 */

#include "weightlearner.h"

const long double PI = 3.141592653589793238463;
const long double wl_priorMean = 0;

int weird = 0;

long double logProb2Prob(long double logProb) {
	if (logProb > 0 && logProb < 0.0000000000001)
		return 1;
	else
		return expl(logProb);
}

lbfgsfloatval_t _evaluate(void* instance, const lbfgsfloatval_t* cur_weights,
		lbfgsfloatval_t *gradient, const int n, const lbfgsfloatval_t step) {
	struct LearningRun* run = (struct LearningRun*) instance;
	lbfgsfloatval_t negll = wlp_evaluate(run->problem, cur_weights, gradient, n,
			step);
	// in addition to the L1 regularization, add the gaussian priors
	return wl_addPrior(run->learner, run->problem, negll, cur_weights, gradient,
			n, step);
}

int _progress(void* instance, const lbfgsfloatval_t *x,
		const lbfgsfloatval_t *g, const lbfgsfloatval_t fx,
		const lbfgsfloatval_t xnorm, const lbfgsfloatval_t gnorm,
		const lbfgsfloatval_t step, int n, int k, int ls) {

	struct LearningRun* run = (struct LearningRun*) instance;
	return wl_progress(run->learner, run->problem, x, g, fx, xnorm, gnorm, step,
			n, k, ls);
}

WeightLearningProblem wlp_new(SddNode* sdd, SddManager* mgr, int nbFeatures,
		int* counts, int nbInstances, long double* weights, int debug) {
	WeightLearningProblem wlp;

	//LikelihoodInferenceProblem constructor
	wlp.sdd = sdd;
	wlp.mgr = mgr;
	wlp.n = nbFeatures;
	wlp.counts = counts;
	wlp.nbInstances = nbInstances;
	wlp.weights = weights;
	wlp.debug = debug;

	wlp.wmcManager = wmc_manager_new(wlp.sdd, 1, wlp.mgr);
	for (int i = 1; i <= sdd_manager_var_count(wlp.mgr); i++) {
		wmc_set_literal_weight(-i, 0, wlp.wmcManager);
	}

	//WeightLearningProblem constructor
	wlp.cnt_wmc = 0;
	wlp.time_wmc1 = 0;
	wlp.time_wmc2 = 0;
	wlp.loglikelihood = INFINITY;

	return wlp;
}

void wlp_free(WeightLearningProblem* wlp) {
	if (wlp->wmcManager != NULL) {
		wmc_manager_free(wlp->wmcManager);
	}
}

lbfgsfloatval_t wlp_get_weight(WeightLearningProblem* wlp,
		const lbfgsfloatval_t* cur_weights, int idx) {
	return cur_weights[idx - 1];
}

long double wlp_infer(WeightLearningProblem* wlp,
		const lbfgsfloatval_t* cur_weights) {
	// set weights in wmcManager
	for (int i = 1; i <= sdd_manager_var_count(wlp->mgr); i++) {
// 		printf("weight %d: %Lf \n",i,  wlp_get_weight(wlp, cur_weights, i));
		wmc_set_literal_weight(i, wlp_get_weight(wlp, cur_weights, i),
				wlp->wmcManager);
		
	}

	// compute WMC and gradiřent
	SddWmc logWmc = wmc_propagate(wlp->wmcManager);
// 	printf("infer: wmc = %f\n",logWmc);
	assert(isfinite(logWmc));
	if(!isfinite(logWmc)){
	  weird = 1;
	  printf("infer: logWmc should be finite but is %f!\n", logWmc);
	  for (int i = 1; i <= sdd_manager_var_count(wlp->mgr); i++) {
		printf("weight %d: %Lf \n",i,  wlp_get_weight(wlp, cur_weights, i));
	  }
	}

	long double negll = wlp->nbInstances * logWmc;
	//int tot = 0;
	for (int i = 0; i < wlp->n; i++) {
		//totř += counts[i];
// 		if(isfinite(cur_weights[i])){//added by jessa
		  negll -= (wlp->counts[i] * cur_weights[i]);
// 		}
// 		} else {//added by jessa
// 		  printf("infer: weight of %d: %Le is infinite\n", i, cur_weights[i]);//added by jessa
// 		}//added by jessa
	}
	//assert(tot==nbInstances);

	assert(negll >= -1e-6);
	if(!isfinite(negll)){
	  weird = 1;
	  printf("infer: negll should be finite!\n");
	}
	assert(isfinite(negll));
	if(negll< -1e-6){
	  weird = 1;
	  printf("infer: negll should be bigger than -1e-6!\n");
	}
	
// 	printf("infer: negll = %Le\n",negll);
	return negll;
}

//long double wlp_inferPerInstance(WeightLearningProblem* wlp,
//		const lbfgsfloatval_t* cur_weights) {
//	return -wlp_infer(wlp, cur_weights) / wlp->nbInstances;
//}
//
//long double wlp_inferPerInstance(WeightLearningProblem* wlp) {
//	return wlp_inferPerInstance(wlp, wlp->weights);
//}

// calculate likelihood and gradient
lbfgsfloatval_t wlp_evaluate(WeightLearningProblem* wlp,
		const lbfgsfloatval_t* cur_weights, lbfgsfloatval_t* gradient,
		const int n, const lbfgsfloatval_t step) {
	clock_t timer1 = clock();
	// get likelihood from super class
	long double negll = wlp_infer(wlp, cur_weights);
	clock_t timer2 = clock();
	// calculate gradient
	for (int i = 0; i < n; i++) {
// 	  if (isfinite(cur_weights[i])){//added by jessa
//        long double marginalProb = logProb2Prob(literal_pr(h[i], wmcManager));
		long double marginalProb = logProb2Prob(
				wmc_literal_pr(i + 1, wlp->wmcManager));
		if(!isfinite(marginalProb)){
		  weird = 1;
		  printf("evaluate: marginalProb should be finite!\n");
		}
		if(marginalProb>1+1e-6){
		  weird = 1;
		  printf("evaluate: marginalProb should be smaller than 1!\n");
		}
		if(marginalProb<-1e-6){
		  weird = 1;
		  printf("evaluate: marginalProb should be bigger than 0!\n");
		}
		assert(isfinite(marginalProb));
		assert(marginalProb<=1+1e-6);
		assert(marginalProb>=-1e-6);
//      if (i!=-3)
		gradient[i] = -(wlp->counts[i] - marginalProb * wlp->nbInstances);
//      else
//        gradient[i] = 0;
		if (wlp->debug) {
//			cout << "P_m(" << i << ") = " << marginalProb << endl;
//			cout << "P_d(" << i << ") = " << (counts[i] * 1.0 / nbInstances)
//					<< endl;
//			cout << "gradient[" << i << "] = " << gradient[i] << endl;
		}
		if(!isfinite(gradient[i])){
		  weird = 1;
		  printf("evaluate: gradient should be finite!\n");
		}
		assert(isfinite(gradient[i]));
// 	  } else {//added by jessa
// 	    gradient[i]=0; //added by jessa
// 	  }//added by jessa
	}
	clock_t timer3 = clock();
	wlp->time_wmc1 += (timer2 - timer1) * 1.0 / CLOCKS_PER_SEC;
	wlp->time_wmc2 += (timer3 - timer2) * 1.0 / CLOCKS_PER_SEC;

	wlp->cnt_wmc = wlp->cnt_wmc + 1;
	return negll;
}

void wlp_done(WeightLearningProblem* wlp, lbfgsfloatval_t *m_x,
		lbfgsfloatval_t fx) {
	for (int i = 0; i < wlp->n; i++) {
		wlp->weights[i] = m_x[i];
	}
	wlp->loglikelihood = -fx / wlp->nbInstances;
}

//WeightLearner wl_new(long double priorSigma = 2, long double l1Const = 0.95,
//		int maxIter = 70, long double delta = 1e-10, long double epsilon = 1e-4) {
WeightLearner wl_new(long double priorSigma, long double l1Const, int maxIter,
		long double delta, long double epsilon) {
	WeightLearner wl;
	wl.priorSigma = priorSigma;
	lbfgs_parameter_init(&wl.settings);
	if (l1Const != 0)
		wl.settings.orthantwise_c = l1Const;
	wl.settings.max_iterations = maxIter;
	wl.settings.delta = delta;
	wl.settings.epsilon = epsilon; // to avoid going to nan?
	wl.numberOfEvaluations = 0;

	return wl;
}

int wl_optimize(WeightLearner* wl, WeightLearningProblem* problem) {

	/* Initialize the variables and settings. */
	lbfgsfloatval_t fx;
	lbfgsfloatval_t *m_x = lbfgs_malloc(problem->n);
	if (m_x == NULL) {
		printf("ERROR: Failed to allocate a memory block for variables.\n");
		return 1;
	}
	for (int i = 0; i < problem->n; i++) {
		m_x[i] = problem->weights[i];
	}
	if (wl->settings.orthantwise_c != 0) {
		wl->settings.linesearch = LBFGS_LINESEARCH_BACKTRACKING; //required by lbfgs lib
		wl->settings.orthantwise_start = 0; //problem->nbUnitFeatures;
		wl->settings.orthantwise_end = problem->n; // incorporate all parameters for non-unit features in L1 norm
	}
	wl->numberOfEvaluations = 0;

	/*
	 Start the L-BFGS optimization; this will invoke the callback functions
	 evaluate() and progress() when necessary.
	 */
	struct LearningRun run;
	run.problem = problem;
	run.learner = wl;
	int ret = lbfgs(problem->n, m_x, &fx, _evaluate, _progress, &run,
			&wl->settings);
	if (problem->debug || ret < 0) {
//          cout << "L-BFGS optimization terminated with status code = " << ret << ", fx = " << fx << endl;
	}
	wlp_done(problem, m_x, fx);
	lbfgs_free(m_x);
	return ret;
}

///**
// * tune parameter of this learner, also updating the trained weights with the new parameters
// */
//long double wl_tuneParameter(WeightLearner* wl,
//		WeightLearningProblem* trainedWeights,
//		WeightLearningProblem* tuningProblem, long double parameter,
//		long double step = 0.9, const char* name = "parameter") {
//
//	// backup original values
//	long double parameterBefore = parameter;
//	long double weightsBefore[trainedWeights->n];
//	for (int jx = 0; jx < trainedWeights->n; jx++) {
//		weightsBefore[jx] = trainedWeights->weights[jx];
//	}
//	long double trainingLoglikelihoodBefore = trainedWeights->loglikelihood;
//
//	// compute initial tuning set score
//	long double tuningLogLikelihoodBefore = wlp_inferPerInstance(tuningProblem);
//
//	// try parameter/step, relearn and reevaluate
//	parameter = parameterBefore / step;
//	if (optimize(trainedWeights) < 0) {
//		perror(
//				"ERROR: Failed to optimize weights while tuning, cannot proceed.");
//		exit(-1);
//	}
//	long double tuningLoglikelihood = wlp_inferPerInstance(tuningProblem,
//			trainedWeights->weights);
//
//	// check for improvement
//	if (tuningLoglikelihood > tuningLogLikelihoodBefore) {
//		printf("Increasing %s from %lf to %lf ( %lf > %lf )", name,
//				parameterBefore, parameter, tuningLoglikelihood,
//				tuningLogLikelihoodBefore);
//		return tuningLoglikelihood;
//	}
//
//	// no improvement, reset weights
//	for (int jx = 0; jx < trainedWeights->n; jx++) {
//		trainedWeights->weights[jx] = weightsBefore[jx];
//	}
//	trainedWeights->loglikelihood = trainingLoglikelihoodBefore;
//
//	// try parameter*step, relearn and reevaluate
//	parameter = parameterBefore * step;
//	if (optimize(trainedWeights) < 0) {
//		perror(
//				"ERROR: Failed to optimize weights while tuning, cannot proceed.");
//		exit(-1);
//	}
//	tuningLoglikelihood = wlp_inferPerInstance(tuningProblem,
//			trainedWeights->weights);
//
//	// check for improvement
//	if (tuningLoglikelihood > tuningLogLikelihoodBefore) {
//		printf("Decreasing %s from %lf to %lf ( %lf > %lf )", name,
//				parameterBefore, parameter, tuningLoglikelihood,
//				tuningLogLikelihoodBefore);
//		return tuningLoglikelihood;
//	}
//
//	// no improvement, reset weights
//	parameter = parameterBefore;
//	for (int jx = 0; jx < trainedWeights->n; jx++) {
//		trainedWeights->weights[jx] = weightsBefore[jx];
//	}
//	trainedWeights->loglikelihood = trainingLoglikelihoodBefore;
//	printf("Keeping %s at %lf ( %lf )", name, parameter,
//			tuningLogLikelihoodBefore);
//
//	return tuningLogLikelihoodBefore;
//}
//
//long double wl_tunePriorSigma(WeightLearner* wl,
//		WeightLearningProblem* trainedWeights,
//		WeightLearningProblem* tuningProblem, long double step = 0.9) {
//	return tuneParameter(trainedWeights, tuningProblem, wl->priorSigma, step,
//			"prior sigma");
//}
//
//long double wl_tunel1Const(WeightLearner* wl,
//		WeightLearningProblem* trainedWeights,
//		WeightLearningProblem* tuningProblem, long double step = 0.9) {
//	return tuneParameter(trainedWeights, tuningProblem,
//			wl->settings.orthantwise_c, step, "l1 constant");
//}
//
//long double wl_tuneParameters(WeightLearner* wl,
//		WeightLearningProblem* trainedWeights,
//		WeightLearningProblem* tuningProblem, long double step = 0.9) {
//	printf("No parameters to tune.");
//	return wlp_inferPerInstance(tuningProblem);
//}
//
//void wl_copyTunedParameters(WeightLearner* wl, WeightLearner* learner) {
//	wl->priorSigma = learner->priorSigma;
//	wl->settings.orthantwise_c = learner->settings.orthantwise_c;
//}

lbfgsfloatval_t wl_addPrior(WeightLearner* wl, WeightLearningProblem* problem,
		lbfgsfloatval_t negll, const lbfgsfloatval_t* cur_weights,
		lbfgsfloatval_t *gradient, const int n, const lbfgsfloatval_t step) {
	if (wl->priorSigma > 0) {
		for (int i = 0; i < n; i++) {
// 		    if (isfinite(cur_weights[i])){ //added by jessa
			negll -= -(cur_weights[i] - wl_priorMean)
					* (cur_weights[i] - wl_priorMean)
					/ (2.0 * wl->priorSigma * wl->priorSigma);
			negll -= -log(wl->priorSigma) - log(2 * PI) / 2.0; //needed to compare different sigmas in parameter tuning, is a constant when comparing different wiehgts
			gradient[i] -= (wl_priorMean - cur_weights[i])
					/ (wl->priorSigma * wl->priorSigma);
			if (problem->debug) {
//               cout << "gaussian (sigma " << priorSigma << ") regularized gradient[" << i << "] += "
//                   << ((cur_weights[i] - priorMean) / (priorSigma * priorSigma )) << " = " << gradient[i] << endl;
			}
			if(!isfinite(gradient[i])){
			  weird = 1;
			  printf("addPrior: gradient should be finite!\n");
			}
			assert(isfinite(gradient[i]));
// 		    }
// 		} else {//added by jessa
// 		  printf("addPrior: weight of %d: %Le is infinite\n", i, cur_weights[i]);//added by jessa
// 		}//added by jessa
		}
		if (problem->debug) {
//			cout << "gaussian regularized negll = " << negll << endl;
		}
		if(!isfinite(negll)){
		  weird = 1;
		  printf("addPrior: negll should be finite!\n");
		}
		assert(isfinite(negll));
	}
	return negll;
}

// report progress per iteration
int wl_progress(WeightLearner* wl, WeightLearningProblem* problem,
		const lbfgsfloatval_t *x, const lbfgsfloatval_t *g,
		const lbfgsfloatval_t fx, const lbfgsfloatval_t xnorm,
		const lbfgsfloatval_t gnorm, const lbfgsfloatval_t step, int n, int k,
		int ls) {
	wl->numberOfEvaluations += ls;
	if (problem->debug) {
		printf("Iteration %d:\n", k);
		printf("  fx = %Lf, ", fx);
		for (int i = 0; i < n; i++) {
			printf("  x[%d] = %Lf,", i, x[i]);
		}
		printf("\n");
		printf("  xnorm = %Lf, gnorm = %Lf, step = %Lf\n", xnorm, gnorm, step);
		printf("\n");
	}
	return 0;
}

void learnWeights_l1(SddNode* sdd, SddManager* mgr, int nbFeatures, int* counts,
		int nbInstances, double* weights, double l1Const) {

	long double newWeights[nbFeatures];
	for (int i = 0; i < nbFeatures; i++){
		newWeights[i] = (long double) weights[i + 1];
// 		printf("%d: %Le -- %d\n",i,newWeights[i],isfinite(newWeights[i]));
	}

// 	for(int i=0; i<nbFeatures; i++) {
// 		printf("%Lf \n", newWeights[i]);
// 	}

	WeightLearningProblem wlp = wlp_new(sdd, mgr, nbFeatures, &counts[1],
			nbInstances, newWeights, 0);

//	printf("Learning weights... \n");
	WeightLearner wl = wl_new(2, (long double) l1Const, 70, 1e-10, 1e-4);
	wl_optimize(&wl, &wlp);
	wlp_free(&wlp);

	for (int i = 0; i < nbFeatures; i++) {
		weights[i + 1] = (double) newWeights[i];
//		printf("%f =?= %Lf \n", weights[i+1], newWeights[i]);
	}
	
	if (weird==1)
	  printf("Something weird happened during weight learning... \n");

// 	for(int i=0; i<nbFeatures; i++) {
// 		printf("%Lf \n", newWeights[i]);
// 	}
// 
// 	for(int i=1; i<=nbFeatures; i++) {
// 		printf("%f \n", weights[i]);
// 	}
}

void learnWeights(SddNode* sdd, SddManager* mgr, int nbFeatures, int* counts,
		int nbInstances, double* weights) {
	learnWeights_l1(sdd,mgr,nbFeatures,counts,nbInstances,weights,0.95);
}

