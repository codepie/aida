package mpi.aida.graph.similarity.measure;

import mpi.aida.data.Entity;
import mpi.experiment.trace.Tracer;

public class UnnormalizedKeyphrasesBasedIDFSimilarity extends UnnormalizedKeyphrasesBasedMISimilarity {

  public UnnormalizedKeyphrasesBasedIDFSimilarity(Tracer tracer) {
    super(tracer);
  }

  protected double getKeywordScore(Entity entity, int keyword) {
    return keyphrasesContext.getKeywordIDFWeight(keyword);
  }

  public String getIdentifier() {
    String identifier = "UnnormalizedKeyphrasesBasedIDFSimilarity";

    if (isUseDistanceDiscount()) {
      identifier += ",i";
    }

    return identifier;
  }
}
