package mpi.aida.graph.similarity.context;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.HashMap;
import java.util.Map;

import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.experiment.trace.GraphTracer;
import mpi.experiment.trace.NullGraphTracer;


public class KeyphraseReweightedKeywordContext extends FastWeightedKeyphrasesContext {

  public KeyphraseReweightedKeywordContext(Entities entities) throws Exception {
    super(entities);
  }
  
  public KeyphraseReweightedKeywordContext(Entities entities, EntitiesContextSettings settings) throws Exception {
    super(entities, settings);
  }

  @Override
  protected TIntObjectHashMap<float[]> fillEntityVectors() {
    TIntObjectHashMap<float[]> vectors = new TIntObjectHashMap<float[]>();

    for (Entity e : entities) {
      float[] weights = new float[allKeywords.size()];

      for (int kp : getEntityKeyphraseIds(e)) {
        for (int tokenId : getKeyphraseTokenIds(kp, true)) { 
          double mi = entity2keyword2mi.get(e.getId()).get(tokenId);
         
          double finalTokenWeight = mi;
          
          double keyphraseWeight = getKeyphraseMiWeight(e, kp);
          double reweightedFinalTokenWeight =  keyphraseWeight * finalTokenWeight;

          if (Double.isNaN(reweightedFinalTokenWeight)) {
            System.err.println("NAN");
          }
          
          weights[tokenId] = (float) reweightedFinalTokenWeight;
        }
      }

      if (!(GraphTracer.gTracer instanceof NullGraphTracer)) {
        Map<String, Float> entityKeywords = new HashMap<String, Float>();

        for (int i = 0; i < weights.length; i++) {
          if (weights[i] > 0.0) {
            entityKeywords.put(getKeywordForId(i), weights[i]);
          }
        }
      }

      vectors.put(e.getId(), weights);
    }
    
    return vectors;
  }
}
