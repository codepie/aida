package mpi.aida.graph.similarity.measure;

import mpi.experiment.trace.Tracer;

public class NormalizedKeyphrasesBasedIDFSimilarity extends UnnormalizedKeyphrasesBasedIDFSimilarity {

  public NormalizedKeyphrasesBasedIDFSimilarity(Tracer tracer) {
    super(tracer);
    normalize = true;
  }
}
