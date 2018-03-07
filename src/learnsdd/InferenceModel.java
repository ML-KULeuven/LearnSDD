package learnsdd;

import data.Data;
import data.Instance;
import sdd.Sdd;
import sdd.SddManager;
import sdd.Vtree;
import sdd.WmcManager;
import utilities.FileParser;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A model that is only used for inference.
 */
public class InferenceModel {

    private Sdd sdd;
    private SddManager manager;
    private ArrayList<Double> weights;

    public InferenceModel(Sdd sdd, SddManager manager, ArrayList<Double> weights) {
        this.sdd = sdd;
        this.manager = manager;
        this.weights = weights;
    }

    public InferenceModel(String sddpath, String weightspath, String vtreepath) {
        try {
            weights = FileParser.readWeights(weightspath);
        } catch (NumberFormatException e) {
            System.err.println("Could not read weights, some of the input were no valid numbers");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Could not read weights");
            e.printStackTrace();
        }

        manager = new SddManager(Vtree.read(vtreepath));
        sdd = Sdd.read(sddpath, manager);

    }



    public void free() {
        manager.free();
    }

    public double getLogLikelihood(Data data) {
        // calculate weighted model count of model
        WmcManager wmcManager = new WmcManager(sdd, true);
        for (int f = 1; f < weights.size(); f++) {
            wmcManager.setLiteralWeight(f, weights.get(f));
        }
        double wmc = wmcManager.propagate();

        // calculate distribution of data= 1/N*sum_{each feature f}(count in
        // data)
        double count = 0;
        for (Instance instance : data) {
            for (int v = 1; v <= data.getNbVars(); v++) {
                wmcManager.setLiteralWeight(instance.get(v) ? -v : v,
                        wmcManager.getZeroWeight());
                wmcManager.setLiteralWeight(instance.get(v) ? v : -v,
                        wmcManager.getOneWeight());
            }
            wmcManager.propagate();
            for (int f = 1; f < weights.size(); f++) {
                if (wmcManager.getProbability(f) > -1000)
                    count += instance.getWeight() * weights.get(f);
            }
        }
        wmcManager.free();
        double LL = count / ((double) data.getNbInstances()) - wmc;
        return LL;
    }

    public long getSizeModel() {
        return sdd.getSize();
    }

    public double getLogLikelihood(String datapath) {
        return getLogLikelihood(new Data(datapath));
    }


}
