/*
 * weightLearnerc.h
 *
 *  Created on: Sep 30, 2014
 *      Author: jessa
 */

#ifndef WEIGHTLEARNERC_H_
#define WEIGHTLEARNERC_H_
#include <stdio.h>
#include <time.h>
#include <math.h>
#include "lbfgs.h"
#include <limits.h>
#include <sddapi.h>

typedef struct WeightLearningProblem WeightLearningProblem;
struct WeightLearningProblem {
	SddNode* sdd;
	SddManager* mgr;
	int n; // num_features
	int* counts;
	int nbInstances;
	long double* weights;
	int debug;
	WmcManager* wmcManager;
	long double loglikelihood;
	int cnt_wmc;
	double time_wmc1;
	double time_wmc2;
};

typedef struct WeightLearner WeightLearner;
struct WeightLearner {
	long double priorSigma;
	lbfgs_parameter_t settings;
	int numberOfEvaluations;
};

struct LearningRun {
	WeightLearningProblem* problem;
	WeightLearner* learner;
};

extern const long double PI;
extern const long double wl_priorMean;

long double logProb2Prob(long double logProb);

lbfgsfloatval_t _evaluate(void* instance,
		const lbfgsfloatval_t* cur_weights, lbfgsfloatval_t *gradient,
		const int n, const lbfgsfloatval_t step);

int _progress(void* instance, const lbfgsfloatval_t *x,
		const lbfgsfloatval_t *g, const lbfgsfloatval_t fx,
		const lbfgsfloatval_t xnorm, const lbfgsfloatval_t gnorm,
		const lbfgsfloatval_t step, int n, int k, int ls);

// constructor weightlearning and likelihoodInference
WeightLearningProblem wlp_new(SddNode* sdd, SddManager* mgr, int nbFeatures,
		int* counts, int nbInstances, long double* weights, int debug);
void wlp_free(WeightLearningProblem* wlp);

// likelihoodInference functions
void wlp_getWeight(WeightLearningProblem* wlp,
		const lbfgsfloatval_t* cur_weights, int idx);
long double wlp_infer(WeightLearningProblem* wlp,
		const lbfgsfloatval_t* cur_weights);
//long double wlp_inferPerInstance(WeightLearningProblem* wlp,
//		const lbfgsfloatval_t* cur_weights);
//long double wlp_inferPerInstance(WeightLearningProblem* wlp);
lbfgsfloatval_t wlp_evaluate(WeightLearningProblem* wlp,
		const lbfgsfloatval_t* cur_weights, lbfgsfloatval_t* gradient,
		const int n, const lbfgsfloatval_t step);
void wlp_done(WeightLearningProblem* wlp, lbfgsfloatval_t *m_x,
		lbfgsfloatval_t fx);

WeightLearner wl_new(long double priorSigma, long double l1Const, int maxIter,
		long double delta, long double epsilon);
int wl_optimize(WeightLearner* wl, WeightLearningProblem* problem);
//long double wl_tuneParameter(WeightLearner* wl,
//		WeightLearningProblem* trainedWeights,
//		WeightLearningProblem* tuningProblem, long double *parameter,
//		long double step = 0.9, const char* name = "parameter");
//long double wl_tunePriorSigma(WeightLearner* wl,
//		WeightLearningProblem* trainedWeights,
//		WeightLearningProblem* tuningProblem, long double step = 0.9);
//long double wl_tunel1Const(WeightLearner* wl,
//		WeightLearningProblem* trainedWeights,
//		WeightLearningProblem* tuningProblem, long double step = 0.9);
//long double wl_tuneParameters(WeightLearner* wl,
//		WeightLearningProblem* trainedWeights,
//		WeightLearningProblem* tuningProblem, long double step = 0.9);
//void wl_copyTunedParameters(WeightLearner* wl, WeightLearner* learner);
lbfgsfloatval_t wl_addPrior(WeightLearner* wl, WeightLearningProblem* problem,
		lbfgsfloatval_t negll, const lbfgsfloatval_t* cur_weights,
		lbfgsfloatval_t *gradient, const int n, const lbfgsfloatval_t step);
int wl_progress(WeightLearner* wl, WeightLearningProblem* problem,
		const lbfgsfloatval_t *x, const lbfgsfloatval_t *g,
		const lbfgsfloatval_t fx, const lbfgsfloatval_t xnorm,
		const lbfgsfloatval_t gnorm, const lbfgsfloatval_t step, int n, int k,
		int ls);

void learnWeights_l1(SddNode* sdd, SddManager* mgr, int nbFeatures, int* counts,
		int nbInstances, double* weights, double l1Const);

void learnWeights(SddNode* sdd, SddManager* mgr, int nbFeatures, int* counts,
		int nbInstances, double* weights);


#endif /* WEIGHTLEARNERC_H_ */
