package mpi.aida.data;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Holds all the keyphrase data describing a set of entities.
 * 
 *
 */
public class Keyphrases {
  
  private TIntObjectHashMap<int[]> entityKeyphrases;
  private TIntObjectHashMap<int[]> keyphraseTokens;
  private TIntObjectHashMap<TIntDoubleHashMap> entity2keyphrase2mi;
  private TIntObjectHashMap<TIntDoubleHashMap> entity2keyword2mi;

  public void setEntityKeyphrases(TIntObjectHashMap<int[]> entityKeyphrases) {
   this.entityKeyphrases = entityKeyphrases;    
  }

  public void setKeyphraseTokens(TIntObjectHashMap<int[]> keyphraseTokens) {
    this.keyphraseTokens = keyphraseTokens;    
  }

  public void setEntityKeyphraseWeights(
      TIntObjectHashMap<TIntDoubleHashMap> entity2keyphrase2mi) {
    this.entity2keyphrase2mi = entity2keyphrase2mi;
  }

  public void setEntityKeywordWeights(
      TIntObjectHashMap<TIntDoubleHashMap> entity2keyword2mi) {
    this.entity2keyword2mi = entity2keyword2mi;
  }

  public TIntObjectHashMap<int[]> getEntityKeyphrases() {
    return entityKeyphrases;
  }

  public TIntObjectHashMap<int[]> getKeyphraseTokens() {
    return keyphraseTokens;
  }

  public TIntObjectHashMap<TIntDoubleHashMap> getEntityKeywordWeights() {
    return entity2keyword2mi;
  }

  public TIntObjectHashMap<TIntDoubleHashMap> getEntityKeyphraseWeights() {
    return entity2keyphrase2mi;
  }
}
