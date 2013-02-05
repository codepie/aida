package mpi.aida.graph.similarity;

import java.util.List;

import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.graph.similarity.util.SimilaritySettings;
import mpi.experiment.trace.Tracer;

public class EnsembleEntityEntitySimilarity {

  private List<EntityEntitySimilarity> eeSims;

  public EnsembleEntityEntitySimilarity(Entities uniqueEntities, SimilaritySettings settings, Tracer tracer) throws Exception {
    eeSims = settings.getEntityEntitySimilarities(uniqueEntities, tracer);
  }

  public double calcSimilarity(Entity a, Entity b) throws Exception {
    double weightedSimilarity = 0.0;

    for (EntityEntitySimilarity eeSim : eeSims) {
      double sim = eeSim.calcSimilarity(a, b) * eeSim.getWeight();
      weightedSimilarity += sim;
    }

    return weightedSimilarity;
  }

  public List<EntityEntitySimilarity> getEeSims() {
    return eeSims;
  }
}
