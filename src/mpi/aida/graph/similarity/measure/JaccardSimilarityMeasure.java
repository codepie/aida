package mpi.aida.graph.similarity.measure;

import gnu.trove.set.hash.TIntHashSet;
import mpi.aida.AidaManager;
import mpi.aida.data.Context;
import mpi.aida.data.Entity;
import mpi.aida.data.Mention;
import mpi.aida.graph.similarity.context.EntitiesContext;
import mpi.experiment.trace.Tracer;

public class JaccardSimilarityMeasure extends MentionEntitySimilarityMeasure {

  public JaccardSimilarityMeasure(Tracer tracer) {
    super(tracer);
  }

  @Override
  public double calcSimilarity(Mention mention, Context context, Entity entity, EntitiesContext entitiesContext) {
    TIntHashSet contextA = new TIntHashSet(context.getTokenIds());
    TIntHashSet contextB = new TIntHashSet(entitiesContext.getContext(entity));

    TIntHashSet union = getUnion(contextA, contextB);
    TIntHashSet intersection = getIntersection(contextA, contextB);

    double jaccardSim = (double) intersection.size() / (double) union.size();   
    return jaccardSim;
  }

  private TIntHashSet getIntersection(TIntHashSet contextA, TIntHashSet contextB) {
    TIntHashSet is = new TIntHashSet();

    for (int a : contextA.toArray()) {
      if (contextB.contains(a) || contextB.contains(AidaManager.expandTerm(a))) {
        is.add(a);
      }
    }

    return is;
  }

  private TIntHashSet getUnion(TIntHashSet contextA, TIntHashSet contextB) {
    TIntHashSet union = new TIntHashSet();

    for (int a : contextB.toArray()) {
      union.add(a);
    }

    for (int a : contextA.toArray()) {
      if (!union.contains(a) && !union.contains(AidaManager.expandTerm(a))) {
        union.add(a);
      }
    }

    return union;
  }
}
