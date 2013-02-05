package mpi.aida.graph.similarity.measure;

import gnu.trove.set.hash.TIntHashSet;

import java.util.HashMap;
import java.util.Map;

import mpi.aida.data.Entity;
import mpi.aida.graph.similarity.context.EntitiesContext;
import mpi.aida.graph.similarity.context.WeightedKeyphrasesContext;
import mpi.experiment.trace.Tracer;

public class WeightedJaccardEntityEntitySimilarityMeasure extends EntityEntitySimilarityMeasure {

  public WeightedJaccardEntityEntitySimilarityMeasure(Tracer tracer) {
    super(tracer);
  }

  @Override
  public double calcSimilarity(Entity a, Entity b, EntitiesContext context) {
    WeightedKeyphrasesContext kpc = (WeightedKeyphrasesContext) context;
       
    TIntHashSet contextA = new TIntHashSet(kpc.getEntityKeyphraseIds(a));
    TIntHashSet contextB = new TIntHashSet(kpc.getEntityKeyphraseIds(b));

    double intersection = getIntersection(a, contextA, b, contextB, kpc);
    double union = getUnion(a, contextA, b, contextB, kpc);

    double jaccardSim = intersection / union;

    return jaccardSim;
  }

  private double getIntersection(Entity a, TIntHashSet contextA, Entity b, TIntHashSet contextB, WeightedKeyphrasesContext kpc) {
    double intersectWeight = 0.0;
    
    for (int k : contextA.toArray()) {
      if (contextB.contains(k)) {
        intersectWeight += Math.min(kpc.getCombinedKeyphraseMiIdfWeight(a, k), kpc.getCombinedKeyphraseMiIdfWeight(b, k));
      }
    }
    
    return intersectWeight;
  }

  private double getUnion(Entity a, TIntHashSet contextA, Entity b, TIntHashSet contextB, WeightedKeyphrasesContext kpc) {
    Map<Integer, Double> weights = new HashMap<Integer, Double>();
    
    for (int k : contextA.toArray()) {
      weights.put(k, kpc.getCombinedKeyphraseMiIdfWeight(a, k));
    }
    
    for (int k : contextB.toArray()) {
      Double kwbWeight = kpc.getCombinedKeyphraseMiIdfWeight(b, k);
      Double kwaWeight = weights.get(k);
      
      if (kwaWeight != null) {
        weights.put(k, Math.max(kwaWeight, kwbWeight));
      } else {
        weights.put(k, kwbWeight);
      }
    }
    
    double unionWeight = 0.0;
    
    for (Double d : weights.values()) {
      unionWeight += d;
    }
    
    return unionWeight;
  }
}
