package utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Parses a weights file
 */
public class FileParser {
	static public ArrayList<Double> readWeights(String weightspath) throws NumberFormatException, IOException {
		ArrayList<Double> weights = new ArrayList<Double>();
		 BufferedReader br;
		br = new BufferedReader(new FileReader(weightspath));
        String line;
		while((line = br.readLine()) != null) {
			if (line.isEmpty())
				continue;
		    weights.add(Double.valueOf(line));
		}
        br.close();
		return weights;
	}
}
