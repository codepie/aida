package mpi.aida.graph.similarity.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MaxMinSettings implements Serializable {

  private static final long serialVersionUID = -3088993650033149824L;

  Map<String, double[]> minMaxs;

  public MaxMinSettings(String propertiesFilePath) 
      throws NumberFormatException, IOException {
    minMaxs = new HashMap<String, double[]>();

    BufferedReader reader = 
        new BufferedReader(new FileReader(propertiesFilePath));
    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
      String[] data = line.split("=");

      double min = Double.parseDouble(data[1].split(" ")[0]);
      double max = Double.parseDouble(data[1].split(" ")[1]);

      minMaxs.put(data[0], new double[] { min, max });
    }
    reader.close();
  }
  
  public MaxMinSettings(Map<String, double[]> minMaxs) {
    this.minMaxs = minMaxs;
  }

  public double getMin(String featureName) {
    if (!minMaxs.containsKey(featureName)) {
      throw new IllegalArgumentException("No min for '"+featureName+"'");
    }
    return minMaxs.get(featureName)[0];
  }

  public double getMax(String featureName) {
    if (!minMaxs.containsKey(featureName)) {
      throw new IllegalArgumentException("No max for '"+featureName+"'");
    }
    return minMaxs.get(featureName)[1];
  }
}
