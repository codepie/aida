package mpi.experiment.trace.measures;


public class GenericEntityEntitySimilarityMeasureTracer extends MeasureTracer {

  public GenericEntityEntitySimilarityMeasureTracer(String name, double weight) {
    super(name, weight);
  }

  @Override
  public String getOutput() {
    return "&nbsp;&nbsp;&nbsp;&nbsp;<em>eesim: " + weight + "</em><br />";
  }

}
