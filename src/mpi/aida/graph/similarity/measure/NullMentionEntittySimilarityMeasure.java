package mpi.aida.graph.similarity.measure;

import mpi.aida.data.Context;
import mpi.aida.data.Entity;
import mpi.aida.data.Mention;
import mpi.aida.graph.similarity.context.EntitiesContext;
import mpi.experiment.trace.Tracer;


public class NullMentionEntittySimilarityMeasure extends MentionEntitySimilarityMeasure {

  public NullMentionEntittySimilarityMeasure(Tracer tracer) {
    super(tracer);
  }

  @Override
  public double calcSimilarity(Mention mention, Context context, Entity entity, EntitiesContext entitiesContext) {
    return 0;
  }
}
