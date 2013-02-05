package mpi.aida.graph.extraction;

import mpi.aida.data.Entities;
import mpi.aida.data.Mentions;
import mpi.aida.graph.similarity.EnsembleEntityEntitySimilarity;


public class ExtractGraphAllEdges extends ExtractGraph {
  
  public ExtractGraphAllEdges(String graphName, Mentions m, Entities ue, EnsembleEntityEntitySimilarity eeSim, double alpha) {
    super(graphName, m, ue, eeSim, alpha);
  }

  protected boolean haveDistinceMentions(String e1, String e2) {
    return true;
  }
}
