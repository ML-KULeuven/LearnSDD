package learnsdd;

import data.Data;
import featurefilter.FeatureFilter;
import featuregenerator.FeatureGenerator;
import logic.BooleanFormula;
import logic.SddFormula;
import org.apache.commons.io.FileUtils;
import sdd.SddManager;
import utilities.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class learns a Markov Network and its SDD simultaneously by iteratively adding features to the model.
 */
public class Learner {

	private final Data trainingData;
	private final Data validationData;
	private final File outDir;
	private final ArrayList<Pair<FeatureGenerator, FeatureFilter>> featureGenerators;
	private final int maxEdges;
	private final boolean verbose;
    private final double alpha;
    private PrintWriter log = null;

    private int overallBestIt;
    private double overallBestValidLL;
    private double overallBestTrainLL;
    private long overallBestSize;

    private int nbIterationsWithoutValidLLImprovement;
    private boolean noChanges;
    private int it;

    private Model model;
    private double currentTrainLL;
    private double currentValidLL;
    private long currentModelSize;
    private double bestScore;

    private BooleanFormula bestFeature;
    private long beginItTime;
    private long endItTime;
    private PrintWriter resultsWriter;

    public Learner(Data trainData, Data validData, File outDir, ArrayList<Pair<FeatureGenerator, FeatureFilter>> featureGenerators, double alpha, int maxEdges, boolean verbose) throws FileNotFoundException {
		this.trainingData = trainData;
		this.validationData = validData;
		this.outDir = outDir;
		this.featureGenerators  = featureGenerators;
        this.alpha= alpha;
		this.maxEdges = maxEdges;
		this.verbose = verbose;
        if (verbose) this.log = new PrintWriter(new File(outDir, "log"));
	}

	public void learnModel() throws Exception {

        initializeOutput();
        beginItTime = System.nanoTime();

        ////////////////////
        // Initialization //
        ////////////////////

        // initialize model
        SddManager manager;
        manager = new SddManager(trainingData.getNbVars(), true);
        SddFormula.setManager(manager);
        model = new Model(trainingData, validationData, manager);
        model.ref();
        model.learnWeights();
        System.out.println("initial nb of features and vars: "
				+ (model.getNbOfFeatures()));
        logprintln("initial nb of features and vars: "
                + (model.getNbOfFeatures()));




        // initialize variables for stop criterion
        nbIterationsWithoutValidLLImprovement = 0;
        noChanges = false;
        it = 0;

        // store relevant information of current model
        currentTrainLL = model.getTrainLogLikelihood();
        currentValidLL = model.getValidLogLikelihood();
        currentModelSize = model.getSize();
        currentModelSize = currentModelSize >0? currentModelSize :1;

        overallBestIt = 0;
        overallBestValidLL = currentValidLL;
        overallBestTrainLL = currentTrainLL;
        overallBestSize = currentModelSize;

        endItTime=System.nanoTime();
        endIteration();

        //////////////////////////
        // INCREMENTAL LEARNING //
        //////////////////////////

        //incrementally improve model until stop criterion is satisfied
        boolean tooManyEdges = false;
        while (!tooManyEdges && !noChanges && nbIterationsWithoutValidLLImprovement<10) {
            beginItTime = System.nanoTime();
            noChanges=true;
            System.out.println();
            System.out.println("Iteration "+it );
            System.out.println("Train LL: " +currentTrainLL);
            System.out.println("Valid LL: " +currentValidLL);
            System.out.println("Size: " +currentModelSize);
            System.out.println("Best Iteration "+overallBestIt );
            System.out.println("Best It Train LL: " +overallBestTrainLL);
            System.out.println("Best It Valid LL: " +overallBestValidLL);
            System.out.println("Best It Size: " +overallBestSize);


            it++;
            bestScore = Double.NEGATIVE_INFINITY;

            logprintln("\n\niteration "+it);





//			generate and filter features to make a candidate set
            bestFeature = null;
            ArrayList<BooleanFormula> candidateFeatures = new ArrayList<BooleanFormula>();
            for (Pair<FeatureGenerator,FeatureFilter> featureGeneratorAndFilter: featureGenerators) {
                FeatureGenerator featureGenerator = featureGeneratorAndFilter.left();
                FeatureFilter filter = featureGeneratorAndFilter.right();
                logprint("generate "+ featureGenerator.toString()+" features...");
                List<? extends BooleanFormula> allFeatures = featureGenerator.generate(model);
                logprintln("done. "+allFeatures.size()+" generated.");
                logprint("filter features...");
                List<? extends BooleanFormula> filtered = filter.filter(allFeatures, model);
                logprintln("done. "+filtered.size()+" kept.");
                candidateFeatures.addAll(filtered);
            }

            // go over all candidate features to find the best one.
            for (BooleanFormula feature: candidateFeatures) {
                logprint(it+"\t"+"test "+feature.toString()+" ");


                Model potentialModel = addFeatureToModel(feature, model);
                double deltaLL = potentialModel.getTrainLogLikelihood() - currentTrainLL;
                double nbEdges = potentialModel.getSize();
                double addedEdges = nbEdges - currentModelSize;
                double score = scoreModel(deltaLL, addedEdges, currentModelSize);
                logprintln("deltaLL: "+deltaLL+", addedEdges: "+addedEdges+", score: "+score);

                if (nbEdges> maxEdges){
                    logprintln("Too many edges, do not consider this feature.");
                }
                else  if (score > bestScore) {
                    bestScore = score;
                    noChanges = false;
                    bestFeature = feature;
                    logprintln("better feature");
                }

            }

			if (bestFeature == null) {
                logprintln("All feature resulted in too many edges: Stop");
                tooManyEdges=true;
            }

            // add best feature permanently to the model
            else {

                // inform the feature generators of the chosen feature
                for (Pair<FeatureGenerator, FeatureFilter> featureGeneratorAndFilter : this.featureGenerators) {
                    featureGeneratorAndFilter.left().informChosenFeature(bestFeature);
                }

                logprintln("Chosen Feature: " + bestFeature.toString());


                // Add the chosen feature permanently to the model
                Model prevModel = model;
                model = addFeatureToModel(bestFeature, model);
                prevModel.deref();
                model.ref();

            }

            // check if the validation set log likelihood has improved

            currentTrainLL = model.getTrainLogLikelihood();
            currentValidLL = model.getValidLogLikelihood();
            currentModelSize = model.getSize();
            currentModelSize = currentModelSize >0? currentModelSize :1;


            endItTime = System.nanoTime();
            endIteration();

            if (currentValidLL > overallBestValidLL){
                nbIterationsWithoutValidLLImprovement = 0;
                overallBestIt = it;
                overallBestValidLL = currentValidLL;
                overallBestTrainLL = currentTrainLL;
                overallBestSize = currentModelSize;
                newOverallBest();
            } else {
                nbIterationsWithoutValidLLImprovement++;
            }

            tooManyEdges &= currentModelSize<maxEdges;

		}

		model.deref();
		model.getManager().free();
	}

    private void initializeOutput() throws IOException {
        resultsWriter = new PrintWriter(new File(outDir,"/results.csv"));
        resultsWriter.println("it, time (s), train LL, valid LL, nb edges");
        resultsWriter.flush();
    }

    private void endIteration() throws FileNotFoundException {
        // write out model statistics
        double time =(endItTime-beginItTime)/1000000000.0;
        logprintln(it+", Time: "+time+", Train LL: "+currentTrainLL+", Valid LL: "+currentValidLL+", Size: "+currentModelSize);
        resultsWriter.println(it+","+time+","+currentTrainLL+","+currentValidLL+","+currentModelSize);
        resultsWriter.flush();

        if (verbose) {
            // write out model
            File itDir = new File(outDir, it+"");
            itDir.mkdir();
            model.getTheory().getSdd().save(new File(itDir,"model.sdd").getAbsolutePath());
            model.getTheory().getSdd().saveAsDot(new File(itDir,"model.dot").getAbsolutePath());
            model.getManager().getVtree().save(new File(itDir,"vtree.vtree").getAbsolutePath());
            model.getManager().getVtree().saveAsDot(new File(itDir,"vtree.dot").getAbsolutePath());
            model.saveWeights(new File(itDir,"weights.csv"));
            model.saveFeatures(new File(itDir,"features.txt"));
        }
    }

    private void newOverallBest() throws IOException {
        File bestDir = new File(outDir, "best");
        if (verbose) {
            //just copy it folder
            FileUtils.copyDirectory(new File(outDir+"/"+it),bestDir);
        } else {
            //replace best model
            if (bestDir.exists())
                FileUtils.deleteDirectory(bestDir);
            bestDir.mkdir();
            model.getTheory().getSdd().save(new File(bestDir,"model.sdd").getAbsolutePath());
            model.getTheory().getSdd().saveAsDot(new File(bestDir,"model.dot").getAbsolutePath());
            model.getManager().getVtree().save(new File(bestDir,"vtree.vtree").getAbsolutePath());
            model.getManager().getVtree().saveAsDot(new File(bestDir,"vtree.dot").getAbsolutePath());
            model.saveWeights(new File(bestDir,"weights.csv"));
            model.saveFeatures(new File(bestDir,"features.txt"));
        }
        PrintWriter writer = new PrintWriter(new File(bestDir, "iteration"));
        writer.println(it);
        writer.close();
    }

    private double scoreModel(double deltaLL, double nbEdgesAdded, long origSize) {
        return deltaLL-alpha*nbEdgesAdded/origSize;
    }


    private Model addFeatureToModel(BooleanFormula feature, Model model) {
        Model potentialModel = model;
        SddManager manager = model.getManager();
        manager.garbageCollect();
        while (manager.getVarCount() > model.getNbOfFeatures()) {
            manager.removeLastAddedVar();
        }
        manager.addVarBeforeLca(feature.getParents());
        potentialModel = potentialModel.addFeature(feature);


        potentialModel.ref();
        potentialModel.getManager().minimize();
        potentialModel.learnWeights();
        potentialModel.deref();

        return potentialModel;
    }

    private void logprintln(String message){
        if (verbose) {
            log.println(message);
            log.flush();
        }
    }
    private void logprint(String message){
        if (verbose) {
            log.print(message);
            log.flush();
        }
    }

}
