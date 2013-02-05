package mpi.experiment.trace.measures;

import java.util.HashMap;
import java.util.Map;

public class TermTracer implements Comparable<TermTracer> {

  double termWeight;

  Map<String, Double> innerMatches = new HashMap<String, Double>();

  public double getTermWeight() {
    return termWeight;
  }

  public void setTermWeight(double termWeight) {
    this.termWeight = termWeight;
  }

  public Map<String, Double> getInnerMatches() {
    return innerMatches;
  }

  public void addInnerMatch(String inner, Double weight) {
    innerMatches.put(inner, weight);
  }

  @Override
  public int compareTo(TermTracer o) {
    return Double.compare(termWeight, o.getTermWeight());
  }
}
