package mpi.aida.graph.similarity.measure;

import mpi.aida.data.Entity;
import mpi.aida.graph.similarity.context.EntitiesContext;
import mpi.experiment.trace.Tracer;

public class NullEntityEntitySimilarityMeasure extends EntityEntitySimilarityMeasure {

  public NullEntityEntitySimilarityMeasure(Tracer tracer) {
    super(tracer);
  }

  @Override
  public double calcSimilarity(Entity a, Entity b, EntitiesContext context) {
    return -1;
  }

}
