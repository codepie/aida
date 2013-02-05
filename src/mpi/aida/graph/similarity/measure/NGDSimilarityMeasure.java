package mpi.aida.graph.similarity.measure;

import gnu.trove.set.hash.TIntHashSet;
import mpi.aida.data.Entity;
import mpi.aida.graph.similarity.context.EntitiesContext;
import mpi.aida.graph.similarity.context.WeightedKeyphrasesContext;
import mpi.aida.util.YagoUtil;
import mpi.experiment.trace.Tracer;

public class NGDSimilarityMeasure extends EntityEntitySimilarityMeasure {

  public NGDSimilarityMeasure(Tracer tracer) {
    super(tracer);
  }

  protected WeightedKeyphrasesContext kwc;

  @Override
  public double calcSimilarity(Entity a, Entity b, EntitiesContext entitiesContext) {
    kwc = (WeightedKeyphrasesContext) entitiesContext;

    double max = getMax(a, b, entitiesContext);
    double min = getMin(a, b, entitiesContext);
    double intersect = getIntersect(a, b, entitiesContext); 
    double collection = getCollection();
    
    double sim = 0.0;
   
    if (intersect > 0) {
      double ngd = 
          ( Math.log(max) - Math.log(intersect) ) 
          / ( Math.log(collection) - Math.log(min) );
      sim = 1 - ngd;      
      if (sim < 0) sim = 0.0;
    }

    return sim;
  }

  protected double getMax(Entity a, Entity b, EntitiesContext entitiesContext) {
    int[] e1context = kwc.getContext(a);
    int[] e2context = kwc.getContext(b);
    
    return Math.max(e1context.length, e2context.length);
  }
  
  protected double getMin(Entity a, Entity b, EntitiesContext entitiesContext) {
    int[] e1context = kwc.getContext(a);
    int[] e2context = kwc.getContext(b);
    
    return Math.min(e1context.length, e2context.length);
  }

  protected double getIntersect(Entity a, Entity b, EntitiesContext entitiesContext) {
    TIntHashSet e1context = new TIntHashSet(kwc.getContext(a));
    TIntHashSet e2context = new TIntHashSet(kwc.getContext(b));
    
    e1context.retainAll(e2context); 
    int intersectSize = e1context.size();
    return (double) intersectSize;
  }

  protected double getCollection() {
    return ((double) YagoUtil.TOTAL_YAGO_ENTITIES);
  }
}
