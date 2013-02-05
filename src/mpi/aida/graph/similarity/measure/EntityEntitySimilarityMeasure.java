package mpi.aida.graph.similarity.measure;

import mpi.aida.data.Entity;
import mpi.aida.graph.similarity.context.EntitiesContext;
import mpi.experiment.trace.Tracer;

public abstract class EntityEntitySimilarityMeasure extends SimilarityMeasure {

  public EntityEntitySimilarityMeasure(Tracer tracer) {
    super(tracer);
  }

  public abstract double calcSimilarity(Entity a, Entity b, EntitiesContext context);
}
