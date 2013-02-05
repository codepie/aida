package mpi.aida.graph.similarity.measure;

import mpi.experiment.trace.Tracer;

public class NormalizedKeyphrasesBasedMISimilarity extends UnnormalizedKeyphrasesBasedMISimilarity {

  public NormalizedKeyphrasesBasedMISimilarity(Tracer tracer) {
    super(tracer);
    normalize = true;
  }
}
