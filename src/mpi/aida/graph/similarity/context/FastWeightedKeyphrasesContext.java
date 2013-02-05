package mpi.aida.graph.similarity.context;

import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javaewah.EWAHCompressedBitmap;
import mpi.aida.access.DataAccess;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.experiment.trace.GraphTracer;
import mpi.experiment.trace.NullGraphTracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the KeyphrasesContext with additional data structures that speed
 * up the computation of keyphrase based coherence (KORE)
 * 
 * @author Johannes Hoffart
 *
 */
public class FastWeightedKeyphrasesContext extends WeightedKeyphrasesContext {
  private static final Logger logger = 
      LoggerFactory.getLogger(FastWeightedKeyphrasesContext.class);
  
  private double keywordCoherenceAlpha = 0.0;
  
  // for fast similarity calculation
  private TIntObjectHashMap<int[]> entity2keywordIds;
  
  /** Contains all keyphrase weights sorted by the weight (not by the
   * initial keyphrase order.
   */
  private TIntObjectHashMap<double[]> entity2combinedMiIdfKeyphraseWeights;
  private TIntObjectHashMap<TIntObjectHashMap<int[]>> entity2keyword2keyphrases;
  private TIntObjectHashMap<TIntDoubleHashMap> entity2keyphrase2keywordWeightSum;
    
  public FastWeightedKeyphrasesContext(Entities entities) throws Exception {
    super(entities);
  }
  
  public FastWeightedKeyphrasesContext(Entities entities, EntitiesContextSettings settings) throws Exception {
    super(entities, settings);
  }
    
  @Override
  protected void setupEntities(Entities entities) throws Exception {
    super.setupEntities(entities);
        
    if (settings != null) {
      keywordCoherenceAlpha = settings.getEntityCoherenceKeywordAlpha();
    }
        
    // create vectors + datastructures for speeding up the calculation    
    entity2keywordIds = createEntity2keywordMapping();
    entity2combinedMiIdfKeyphraseWeights = createEntity2combinedMiIdfKeyphraseWeightsMapping();
    entity2keyword2keyphrases = createEntity2keyword2keyphrasesMapping();
    entity2keyphrase2keywordWeightSum = createEntity2keyphrase2keywordWeightSumMapping();
        
    logger.info("FastWeightedKeyphrasesContext setup done");
  }
  
  private TIntObjectHashMap<TIntDoubleHashMap> createEntity2keyphrase2keywordWeightSumMapping() {
    TIntObjectHashMap<TIntDoubleHashMap> m = new TIntObjectHashMap<TIntDoubleHashMap>();
	  for (Entity e : entities) {
		  m.put(e.getId(),  new TIntDoubleHashMap());
		  for (int kp : getEntityKeyphraseIds(e)) {
			  double keywordWeightSum = .0;
			  for (int kw : getKeyphraseTokenIds(kp, true)) {
				  keywordWeightSum += getCombinedKeywordMiIdfWeight(e, kw);
			  }
			  m.get(e.getId()).put(kp, keywordWeightSum);
		  }
	  }
	  return m;
  }
  
  private TIntObjectHashMap<TIntObjectHashMap<int[]>> createEntity2keyword2keyphrasesMapping() {
    TIntObjectHashMap<TIntObjectHashMap<int[]>> m = new  TIntObjectHashMap<TIntObjectHashMap<int[]>>();
	  for (Entity e : entities) {
		  TIntObjectHashMap<TIntHashSet> kw2kp = new TIntObjectHashMap<TIntHashSet>();
		  for (int kp : getEntityKeyphraseIds(e)) {
			  for (int kw : getKeyphraseTokenIds(kp, true)) {
				  if (!kw2kp.contains(kw)) {
					  kw2kp.put(kw, new TIntHashSet());
				  }
				  kw2kp.get(kw).add(kp);
			  }
		  }
		  TIntObjectHashMap<int[]> kw2kp_array = new TIntObjectHashMap<int[]>();
		  for (int kw : kw2kp.keys()) {
			  kw2kp_array.put(kw, kw2kp.get(kw).toArray());
		  }
		  m.put(e.getId(), kw2kp_array);
	  }
	  return m;
  }
  
  /**
   * This will order the weight vectors by weight, not by the original 
   * keyphrase position.
   */
  private TIntObjectHashMap<double[]> createEntity2combinedMiIdfKeyphraseWeightsMapping() {
    TIntObjectHashMap<double[]> m = new TIntObjectHashMap<double[]>();
	  for (Entity e : entities) {
		  int[] kps = getEntityKeyphraseIds(e);		  
		  double[] weights = new double[kps.length];
		  for (int i = 0; i < kps.length; ++i) {
			  weights[i] = getCombinedKeyphraseMiIdfWeight(e, kps[i]);
		  }
		  Arrays.sort(weights);
		  m.put(e.getId(), weights);
	  }
	  return m;
  }
  
  private TIntObjectHashMap<int[]> createEntity2keywordMapping() {
    TIntObjectHashMap<int[]> m = new TIntObjectHashMap<int[]>();
	  for (Entity e : entities) {
		  TIntHashSet set = new TIntHashSet();
		  for (int kp : getEntityKeyphraseIds(e)) {
			  for (int t : getKeyphraseTokenIds(kp, true)) {
				  set.add(t);
			  }
		  }
		  int[] kw = set.toArray();
		  Arrays.sort(kw);
		  m.put(e.getId(), kw);
	  }
	  return m;
  }

  protected TIntObjectHashMap<float[]> fillEntityVectors() {   
    TIntObjectHashMap<float[]> vectors = new TIntObjectHashMap<float[]>();

    for (Entity e : entities) {
      float[] weights = new float[allKeywords.size()];

      for (int kp : eKps.get(e.getId())) {
        for (int tokenId : kpTokens.get(kp)) {
          double mi = entity2keyword2mi.get(e.getId()).get(tokenId);
          double idf = keyword2idf.get(tokenId);
          
          float finalTokenWeight = (float) ((keywordCoherenceAlpha * mi) + ((1-keywordCoherenceAlpha) * idf));
          
          weights[tokenId] = finalTokenWeight;
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

  @SuppressWarnings("unused")
  private TIntDoubleHashMap calculateVectorNorms(TIntObjectHashMap<float[]> vectors) {
    TIntDoubleHashMap norms = new TIntDoubleHashMap();
        
    for (int e : vectors.keys()) {
      double norm = 0.0;
      for (float f : vectors.get(e)) {
        norm += f * f;
      }
      norms.put(e, Math.sqrt(norm));
    }
    return norms;
  }

  @SuppressWarnings("unused")
  private TIntObjectHashMap<EWAHCompressedBitmap> createBitMaps(TIntObjectHashMap<float[]> vectors) {
    TIntObjectHashMap<EWAHCompressedBitmap> bitMaps = new TIntObjectHashMap<EWAHCompressedBitmap>();
    
    for (int e : vectors.keys()) {
      EWAHCompressedBitmap bm = new EWAHCompressedBitmap();
      bitMaps.put(e, bm);
      
      float[] values = vectors.get(e);
      for (int i=0;i<values.length;i++) {
        if (values[i] > 0.0) {
          bm.set(i);
        }
      }
    }
    
    return bitMaps;
  }

  @SuppressWarnings("unused")
  private TIntObjectHashMap<EWAHCompressedBitmap> createKeyphraseBitMaps() {    
    TIntObjectHashMap<EWAHCompressedBitmap> bitMaps = new TIntObjectHashMap<EWAHCompressedBitmap>();

    for (int kp : kpTokens.keys()) {
      EWAHCompressedBitmap bm = new EWAHCompressedBitmap();
      
      int[] keywordIds = getKeyphraseTokenIds(kp, true);
      Arrays.sort(keywordIds);
      
      for (int id : keywordIds) {
        bm.set(id);
      }
      
      bitMaps.put(kp, bm);
    }
    
    return bitMaps;
  }

  @Override
  public int[] getContext(Entity entity) {
    TIntLinkedList keywords = new TIntLinkedList();    
    for (int keyphrase : eKps.get(entity.getId())) {
      for (int keyword : kpTokens.get(keyphrase))
      keywords.add(keyword);
    }    
    return keywords.toArray();
  }
  
  public double getCombinedKeywordMiIdfWeight(Entity entity, int keyword) {
    double mi = getKeywordMIWeight(entity, keyword);
    double idf = getKeywordIDFWeight(keyword);
    double kwWeight = (keywordCoherenceAlpha * mi) + ((1-keywordCoherenceAlpha) * idf);
    return kwWeight;
  }
  
  public double[] getKeyphraseWeights(Entity entity) {
	  return entity2combinedMiIdfKeyphraseWeights.get(entity.getId());
  }
  
  public double getKeywordWeightSum(Entity entity, int keyphrase) {
	  return entity2keyphrase2keywordWeightSum.get(entity.getId()).get(keyphrase);
  }

  public int[] getKeywordArray(Entity entity) {
	  return entity2keywordIds.get(entity.getId());
  }
  
  public int[] getKeyphrasesForKeyword(Entity entity, int token) {
	  return entity2keyword2keyphrases.get(entity.getId()).get(token);
  }
    
  public int[] getKeyphraseTokenIds(int keyphraseId, boolean removeStopwords) {
    int[] tokenIds = null;
    
    if (removeStopwords) {
      tokenIds = kpTokensNoStopwords.get(keyphraseId);
    } else {
      tokenIds = kpTokens.get(keyphraseId);
    }    
    
    return tokenIds;
  }
  
  public String toString() {
    return getIdentifier();
  }

  public String getKeywordForId(int kwId) {
    return DataAccess.getWordForId(kwId);
  }
}
