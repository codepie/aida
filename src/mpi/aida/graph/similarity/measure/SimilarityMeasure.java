package mpi.aida.graph.similarity.measure;

import mpi.experiment.trace.Tracer;

public abstract class SimilarityMeasure {

  protected Tracer tracer = null;

  public SimilarityMeasure(Tracer tracer) {
    this.tracer = tracer;
  }

  public String toString() {
    return getIdentifier();
  }

  public String getIdentifier() {
    String id = this.getClass().getSimpleName();
    return id;
  }
  
  public Tracer getTracer() {
    return tracer;
  }
}
