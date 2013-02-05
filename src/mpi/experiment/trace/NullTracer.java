package mpi.experiment.trace;

import mpi.aida.data.Mention;
import mpi.experiment.trace.data.EntityTracer;
import mpi.experiment.trace.data.MentionTracer;
import mpi.experiment.trace.measures.MeasureTracer;

public class NullTracer extends Tracer {

  EntityEntityTracing nullEETracing = new NullEntityEntityTracing();
  
  public NullTracer() {
    super(null, null);
  }

  public void addMentionForDocId(String docId, Mention m, MentionTracer mt) {
  }

  public void addEntityForMention(Mention mention, String entity, EntityTracer entityTracer) {
  }

  public void addMeasureForMentionEntity(Mention mention, String entity, MeasureTracer measure) {
  }

  public void setMentionEntityTotalSimilarityScore(Mention mention, String entity, double score) {
  }

  public void writeOutput(String resultFileName, boolean withYago) {
  }
  
  public EntityEntityTracing eeTracing() {
    return nullEETracing;
  }
}
