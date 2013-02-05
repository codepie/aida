package mpi.aida.graph.similarity.measure;

import gnu.trove.set.hash.TIntHashSet;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import mpi.aida.data.Entity;
import mpi.aida.graph.similarity.context.EntitiesContext;
import mpi.aida.graph.similarity.context.WeightedKeyphrasesContext;
import mpi.aida.util.CollectionUtils;
import mpi.experiment.trace.NullEntityEntityTracing;
import mpi.experiment.trace.Tracer;
import mpi.experiment.trace.measures.KeytermEntityEntityMeasureTracer;
import mpi.experiment.trace.measures.TermTracer;

public class KeyphraseCosineSimilarityMeasure extends EntityEntitySimilarityMeasure {

  public KeyphraseCosineSimilarityMeasure(Tracer tracer) {
    super(tracer);
  }

  private WeightedKeyphrasesContext kpc;

  @Override
  public double calcSimilarity(Entity a, Entity b, EntitiesContext entitiesContext) {
    kpc = (WeightedKeyphrasesContext) entitiesContext;
    
    Map<String, Double> matches = new HashMap<String, Double>();
    double dotprod = 0.0;

    int[] e1kps = kpc.getEntityKeyphraseIds(a);
    int[] e2kps = kpc.getEntityKeyphraseIds(b);
    
    TIntHashSet e1kpsForIntersect = new TIntHashSet(e1kps);
    TIntHashSet e2kpsForIntersect = new TIntHashSet(e2kps);
    e1kpsForIntersect.retainAll(e2kpsForIntersect);
        
    // iterate through intersection
    for (int kp :  e1kpsForIntersect.toArray()) {     
      double v1 = kpc.getCombinedKeyphraseMiIdfWeight(a, kp);
      double v2 = kpc.getCombinedKeyphraseMiIdfWeight(b, kp);

      if (v1 > 0 && v2 > 0) {
        double tmp = v1 * v2;
        dotprod += tmp;
        
        matches.put(kpc.getKeyphraseForId(kp), tmp);
      }
    }

    double norm1 = calcNorm(a, e1kps);
    double norm2 = calcNorm(b, e2kps);
    
    double sim = 0.0;
    double denom = norm1 * norm2;

    if (denom != 0) {
      sim = dotprod / denom;
    }
    
    if (!(tracer.eeTracing() instanceof NullEntityEntityTracing)) {
      Map<String, Double> e1keyphrases = new HashMap<String, Double>();     
      for (int kp : e1kps) {
        if (kpc.getCombinedKeyphraseMiIdfWeight(a, kp) > 0.0) {
          e1keyphrases.put(kpc.getKeyphraseForId(kp), kpc.getCombinedKeyphraseMiIdfWeight(a, kp));
        }
      }     
      e1keyphrases = CollectionUtils.sortMapByValue(e1keyphrases, true);
      Map<String, Double> e1top = new LinkedHashMap<String, Double>();           
      for (Entry<String, Double> e : e1keyphrases.entrySet()) {       
        e1top.put(e.getKey(), e.getValue());
      }      
      e1keyphrases = e1top;
      
      
      Map<String, Double> e2keyphrases = new HashMap<String, Double>();
      for (int kp : e2kps) {
        if (kpc.getCombinedKeyphraseMiIdfWeight(b, kp) > 0.0) {
          e2keyphrases.put(kpc.getKeyphraseForId(kp), kpc.getCombinedKeyphraseMiIdfWeight(b, kp));
        }
      }  
      e2keyphrases = CollectionUtils.sortMapByValue(e2keyphrases, true);
      Map<String, Double> e2top = new LinkedHashMap<String, Double>();           
      for (Entry<String, Double> e : e2keyphrases.entrySet()) {       
        e2top.put(e.getKey(), e.getValue());
      }      
      e2keyphrases = e2top;
      
      
      Map<String, TermTracer> matchedKeywords = new HashMap<String, TermTracer>();
      for (String kp : matches.keySet()) {
        TermTracer tt = new TermTracer();
        tt.setTermWeight(matches.get(kp));
        matchedKeywords.put(kp, tt);
      }
      
      tracer.eeTracing().addEntityContext(a.getName(), e1keyphrases);
      tracer.eeTracing().addEntityContext(b.getName(), e2keyphrases);

      KeytermEntityEntityMeasureTracer mt = new KeytermEntityEntityMeasureTracer("KeyphraseCosineSim", 0.0, e2keyphrases, matchedKeywords);
      mt.setScore(sim);
      tracer.eeTracing().addEntityEntityMeasureTracer(a.getName(), b.getName(), mt);
      
      KeytermEntityEntityMeasureTracer mt2 = new KeytermEntityEntityMeasureTracer("KeyphraseCosineSim", 0.0, e1keyphrases, matchedKeywords);
      mt2.setScore(sim);
      tracer.eeTracing().addEntityEntityMeasureTracer(b.getName(), a.getName(), mt2);
    }
    
    return sim;
  }

  private double calcNorm(Entity entity, int[] kps) {
    double tmpNorm = 0.0;
    
    for (int keyphrase : kps) {
      double kpWeight = kpc.getCombinedKeyphraseMiIdfWeight(entity, keyphrase);
      tmpNorm += kpWeight * kpWeight;
    }
    
    return Math.sqrt(tmpNorm);
  }
}
