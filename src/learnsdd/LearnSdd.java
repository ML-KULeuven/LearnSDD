package learnsdd;

import data.Data;
import featurefilter.ConjKLDscorer;
import featurefilter.FeatureFilter;
import featurefilter.PartialFilter;
import featurefilter.RandomFilter;
import featuregenerator.ConjunctiveFeatureGenerator;
import featuregenerator.FeatureGenerator;
import featuregenerator.MutexFeatureGenerator;
import org.apache.commons.io.FileUtils;
import utilities.Pair;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class parses the input arguments and starts the learning or inference with the right options.
 */
public class LearnSdd {

	public static void main(String[] args) {
		if (args.length > 0) {
            try {
                if (args[0].contains("learn")) {


                    String[] requiredArgs = new String[]{"trainData", "validData", "outDir"};
                    String[] optionalArgs = new String[]{"alpha", "maxEdges", "features", "verbose"};
                    HashMap<String, String> defaults = new HashMap<String, String>();
                    HashMap<String, String> helps = new HashMap<String, String>();
                    helps.put("trainData", "Path to the training data file.");
                    helps.put("validData", "Path to the validation data file.");
                    helps.put("outDir", "Path to the output directory.");
                    helps.put("alpha", "The trade-off parameter of the scoring function, default = 0.1.");
                    defaults.put("alpha", "0.1");
                    helps.put("maxEdges", "The maximum number of edges for the learned SDD, default = 10^9");
                    defaults.put("maxEdges", "1000000000");
                    helps.put("features", "The types of features that are generated (conj, mutex or conj-mutex), default = conj.");
                    defaults.put("features", "conj");
                    helps.put("verbose", "If this flag is set, all the intermediate models and more log files are saved.");
                    defaults.put("verbose", null);


                    try {
                        HashMap<String, String> argsmap = parseArgs(args, requiredArgs, defaults);

                        System.out.println("Arguments:");
                        for (String key : argsmap.keySet()) {
                            System.out.println(key + ":\t" + argsmap.get(key));
                        }
                        System.out.println();

                        Data trainData = new Data(argsmap.get("trainData"));
                        Data validData = new Data(argsmap.get("validData"));

                        ArrayList<Pair<FeatureGenerator, FeatureFilter>> featureGenerators = new ArrayList<Pair<FeatureGenerator, FeatureFilter>>();
                        int nbFeatures = 200;

                        if (argsmap.get("features").contains("conj")) {
                            FeatureGenerator generator = new ConjunctiveFeatureGenerator();
                            featureGenerators.add(new Pair<FeatureGenerator, FeatureFilter>(generator, new PartialFilter(nbFeatures, new ConjKLDscorer())));
                        }
                        if (argsmap.get("features").contains("mutex")) {
                            FeatureGenerator generator = new MutexFeatureGenerator(1.0);
                            featureGenerators.add(new Pair<FeatureGenerator, FeatureFilter>(generator, new RandomFilter(nbFeatures)));
                        }

                        boolean verbose = (argsmap.get("verbose") != null);
                        File outDir = new File(argsmap.get("outDir"));
                        try {
                            if (outDir.exists()) {
                                FileUtils.deleteDirectory(outDir);
                            }
                            outDir.mkdir();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        PrintWriter writer = new PrintWriter(new File(outDir, "arguments.txt"));
                        writer.print("LearnSdd");
                        for (String arg : args) writer.print(" " + arg);
                        writer.println("\n");
                        for (String key : argsmap.keySet()) {
                            writer.println(key + ":\t" + argsmap.get(key));
                        }
                        writer.close();

                        Learner learner = new Learner(trainData, validData, outDir, featureGenerators, Double.parseDouble(argsmap.get("alpha")), Integer.parseInt(argsmap.get("maxEdges")), verbose);
                        try {
                            learner.learnModel();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        System.out.println(makeHelpString("LearnSdd learn", requiredArgs, optionalArgs, helps, defaults));
                    }

                } else if (args[0].contains("infer")) {


                    String[] requiredArgs = new String[]{"sddPath", "vtreePath", "weightsPath", "dataPath"};
                    String[] optionalArgs = new String[0];
                    HashMap<String, String> defaults = new HashMap<String, String>();
                    HashMap<String, String> helps = new HashMap<String, String>();
                    helps.put("sddPath", "Path to the sdd file.");
                    helps.put("vtreePath", "Path to the vtree file.");
                    helps.put("weightsPath", "Path to the weights file.");
                    helps.put("dataPath", "Path to the data file.");


                    try {
                        HashMap<String, String> argsmap = parseArgs(args, requiredArgs, defaults);

                        InferenceModel inferenceModel = new InferenceModel(argsmap.get("sddPath"), argsmap.get("weightsPath"), argsmap.get("vtreePath"));

                        System.out.println("Size\t: " + inferenceModel.getSizeModel());
                        System.out.println("LL:\t" + inferenceModel.getLogLikelihood(argsmap.get("dataPath")));
                    } catch (Exception e){
                        System.out.println("wrong input arguments: ");
                        System.out.print("LearnSdd");
                        for (String arg: args) System.out.print(" "+arg);
                        System.out.println();
                        System.out.println(makeHelpString("LearnSdd infer", requiredArgs, optionalArgs, helps, defaults));
                    }

                }
            } catch (Exception e) {
                printHelp();
            }
        }
        else {
                printHelp();
            }
	}

    private static String makeHelpString(String cmd, String[] requiredArgs, String[] optionalArgs, HashMap<String, String> helps, HashMap<String, String> defaults) {
        String str = "Usage: "+cmd;
        for (String arg : requiredArgs){
            str += " <"+arg+">";
        }
        for (String arg : optionalArgs){
            str += " [--"+arg+(defaults.get(arg)==null?"":" "+arg.toUpperCase())+"]";
        }
        str+="\n\nRequired arguments:\n";
        for (String arg : requiredArgs){
            str += "\t"+arg+"\t"+helps.get(arg)+"\n";
        }
        str+="\nOptional arguments:\n";
        for (String arg : optionalArgs){
            str += "\t--"+arg+"\t"+helps.get(arg)+"\n";
        }

        return str;
    }

    private static HashMap<String, String> parseArgs(String[] args, String[] requiredArgs, HashMap<String, String> defaults) {
        HashMap<String, String> argsmap = new HashMap<String, String>(defaults); // initialize arguments with their default value
        String key="";
        String value = null;
        int i = 1;

        //Required Arguments
        for (String arg: requiredArgs){
            value = args[i];
            argsmap.put(arg, value);

            i++;
        }

        key = null;
        value = "";

        //Optional arguments
        while (i < args.length){
            if (args[i].startsWith("--")) {
                if (key!=null) {
                argsmap.put(key, value);
                }
                key = args[i].substring(2,args[i].length());
                value = "";
            }
            else {
                value=args[i];
            }
            i++;
        }
        if (key!=null) {
            argsmap.put(key, value);
        }
        return argsmap;
    }

    private static void printHelp(){
        System.out.println("-----------------------------------------");
        System.out.println("              LearnSDD HELP              ");
        System.out.println("-----------------------------------------");
        System.out.println();
        System.out.println("Learning");
        System.out.println("--------");
        System.out.println("LearnSdd learn <traindatapath> <validdatapath> <outdir> [OPTION]*");
        System.out.println("Execute \"LearnSdd learn\" for all options and more details.");
        System.out.println();
        System.out.println();
        System.out.println("Inference");
        System.out.println("---------");
        System.out.println("LearnSdd infer <sddpath> <vtreepath> <weightspath> <datapath>");
    }

}
