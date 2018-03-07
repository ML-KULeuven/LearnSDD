/*
 * wltest.c
 *
 *  Created on: Oct 2, 2014
 *      Author: jessa
 */

#include "weightlearner.h"

int main(int argc, char** argv) {

	SddManager* mgr = sdd_manager_create(3, 0);
	SddNode* fvar = sdd_manager_literal(3, mgr);
	SddNode* fmean = sdd_conjoin(sdd_manager_literal(1, mgr),
			sdd_manager_literal(2, mgr), mgr);
	SddNode* sdd = sdd_conjoin(sdd_disjoin(sdd_negate(fvar, mgr), fmean, mgr),
			sdd_disjoin(fvar, sdd_negate(fmean, mgr), mgr), mgr);

	int nbFeatures = 3;
	int counts[4] = { 0, 90000000, 30000000, 30000000 };
	int nbInstances = 100000000;
	double weights[4] = { 0, 5.1, 0, 0 };

	learnWeights(sdd, mgr, nbFeatures, counts, nbInstances, weights);

	printf("weights \n");
	for (int i = 1; i <= nbFeatures; i++) {
		printf("%f \n", weights[i]);
	}
	printf("\n");

	WmcManager* wmc = wmc_manager_new(sdd, 1, mgr);
	for (int i = 1; i <= nbFeatures; i++) {
		wmc_set_literal_weight(i, weights[i], wmc);
	}
	wmc_propagate(wmc);

	printf("probabilities \n");
	for (int i = 1; i <= nbFeatures; i++) {
		printf("%f \n", wmc_literal_pr(i, wmc));
	}
	
	
	return 0;
}
