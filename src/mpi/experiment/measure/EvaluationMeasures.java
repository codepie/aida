package mpi.experiment.measure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EvaluationMeasures {
  public static Map<String, Double> convertToAverageRanks(List<List<String>> list) {
    Map<String, Double> rankedList = new HashMap<String, Double>();

    int i=0;
    for (List<String> entityPartition : list) {
      double avgRank = 0.0;
      
      for (@SuppressWarnings("unused") String entity : entityPartition) {
        i++;
        avgRank += i;
      }
      
      avgRank /= (double) entityPartition.size();
      
      for (String entity : entityPartition) {
        rankedList.put(entity, avgRank);
      }
    }
    
    return rankedList;
  }
}
