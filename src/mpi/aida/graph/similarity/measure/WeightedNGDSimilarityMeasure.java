package mpi.aida.graph.similarity.measure;

import gnu.trove.set.hash.TIntHashSet;
import mpi.aida.data.Entity;
import mpi.aida.graph.similarity.context.EntitiesContext;
import mpi.aida.util.YagoUtil;
import mpi.experiment.trace.Tracer;


public class WeightedNGDSimilarityMeasure extends NGDSimilarityMeasure {

  public WeightedNGDSimilarityMeasure(Tracer tracer) {
    super(tracer);
  }

  @Override
  protected double getMax(Entity a, Entity b, EntitiesContext entitiesContext) {
    int[] e1context = kwc.getEntityKeyphraseIds(a);
    int[] e2context = kwc.getEntityKeyphraseIds(b);
    
    double e1weight = 0.0;
    for (int kp : e1context) {
      e1weight += kwc.getCombinedKeyphraseMiIdfWeight(a, kp);
    }  
    
    double e2weight = 0.0;
    for (int kp : e2context) {
      e2weight += kwc.getCombinedKeyphraseMiIdfWeight(b, kp);
    }  
    
    return Math.max(e1weight, e2weight);
  }

  @Override
  protected double getMin(Entity a, Entity b, EntitiesContext entitiesContext) {
    int[] e1context = kwc.getEntityKeyphraseIds(a);
    int[] e2context = kwc.getEntityKeyphraseIds(b);
    
    double e1weight = 0.0;
    for (int kp : e1context) {
      e1weight += kwc.getCombinedKeyphraseMiIdfWeight(a, kp);
    }  
    
    double e2weight = 0.0;
    for (int kp : e2context) {
      e2weight += kwc.getCombinedKeyphraseMiIdfWeight(b, kp);
    }  
    
    return Math.min(e1weight, e2weight);
  }

  @Override
  protected double getIntersect(Entity a, Entity b, EntitiesContext entitiesContext) {
    int[] e1context = kwc.getEntityKeyphraseIds(a);
    int[] e2context = kwc.getEntityKeyphraseIds(b);
    
    TIntHashSet e1forIntersect = new TIntHashSet(e1context);
    TIntHashSet e2forIntersect = new TIntHashSet(e2context);
    e1forIntersect.retainAll(e2forIntersect);
    
    double intersectWeight = 0.0;
    
    for (int kp : e1forIntersect.toArray()) {
      intersectWeight += kwc.getCombinedKeyphraseMiIdfWeight(a, kp);
      intersectWeight += kwc.getCombinedKeyphraseMiIdfWeight(b, kp);
    }
    
    // everthing was counted twice
    intersectWeight /= 2;
    
    return intersectWeight;
  }

  @Override
  protected double getCollection() {
    return YagoUtil.TOTAL_YAGO_ENTITIES;
  }
}
