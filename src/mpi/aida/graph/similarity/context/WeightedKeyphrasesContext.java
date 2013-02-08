package mpi.aida.graph.similarity.context;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mpi.aida.access.DataAccess;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.graph.similarity.measure.WeightComputation;
import mpi.aida.util.WikipediaUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the KeyphrasesContext with keyphrase MI/IDF weights
 * 
 *
 */
public class WeightedKeyphrasesContext extends KeyphrasesContext {
  private static final Logger logger = 
      LoggerFactory.getLogger(WeightedKeyphrasesContext.class);
    
  private TIntDoubleHashMap keyphraseIDFweights;
  
  private double keyphraseCoherenceAlpha = 1.0;
      
  public WeightedKeyphrasesContext(Entities entities) throws Exception {
    this(entities, new EntitiesContextSettings());
  }
  
  public WeightedKeyphrasesContext(Entities entities, EntitiesContextSettings settings) throws Exception {
    super(entities, settings);
  }
  
  public double getCombinedKeyphraseMiIdfWeight(Entity entity, int keyphraseId) {
    double kpMI = getKeyphraseMiWeight(entity, keyphraseId);
    double kpIDF = getKeyphraseIdfWeight(keyphraseId);
    double comb = (keyphraseCoherenceAlpha * kpMI) + (1-keyphraseCoherenceAlpha) * kpIDF; 
    return comb;
  }

  public double getKeyphraseIdfWeight(int keyphraseId) {
    if (keyphraseIDFweights.contains(keyphraseId)) {
      return keyphraseIDFweights.get(keyphraseId);
    } else {
      logger.debug("No Keyphrase DF count for '"+getKeyphraseForId(keyphraseId)+"'");      
      return 0;
    }    
  }
  
  public double getKeyphraseMiWeight(Entity entity, int keyphraseId) {
    TIntDoubleHashMap kpMI = entity2keyphrase2mi.get(entity.getId());
    if ((kpMI != null) && kpMI.containsKey(keyphraseId)) {
      return kpMI.get(keyphraseId);      
    } else {
      logger.debug("No count for '"+entity+"'/'"+getKeyphraseForId(keyphraseId)+"' ("+keyphraseId+")");      
      return 0;
    }
  }
  
  public double getAverageKeyphraseMiWeight(Entity a, Entity b, int keyphraseId) {
    return ((entity2keyphrase2mi.get(a.getId()).get(keyphraseId) + 
        entity2keyphrase2mi.get(b.getId()).get(keyphraseId)) / 2);
  }
  
  @Override
  protected void setupEntities(Entities entities) throws Exception {
    super.setupEntities(entities);
    
    boolean shouldNormalizeWeights = true;
    if (settings != null) {
      shouldNormalizeWeights = settings.shouldNormalizeWeights();
    }
    
    if (settings != null) {
      keyphraseCoherenceAlpha = settings.getEntityCoherenceKeyphraseAlpha();
    }
    
    // Setup MI/IDF weights
    logger.debug("Calculating all keyphrase IDF weights");
    
    // compute weights for real entities 
    // IDF
    keyphraseIDFweights = new TIntDoubleHashMap();    
    TIntIntHashMap superDocKeyphraseCounts = DataAccess.getKeyphraseDocumentFrequencies(allKeyphrases);
    calcAndAddIdfWeights(superDocKeyphraseCounts, WikipediaUtil.TOTAL_DOCS, shouldNormalizeWeights, keyphraseIDFweights);   
    
    // do average (for mi-idf comparability) and normalization (so that everything sums up to 1)
    if ((settings != null) && settings.shouldAverageWeights()) {
      entity2keyphrase2mi = averageMIweights(entity2keyphrase2mi);
      keyphraseIDFweights = averageIDFweights(keyphraseIDFweights);
    }

    logger.debug("WeightedKeyphraseContext setup done for " +
        entities.uniqueNameSize() + " entities, " +
        keyphraseIDFweights.size() + " keyphrases");  
  }

  private TIntObjectHashMap<TIntDoubleHashMap> averageMIweights(TIntObjectHashMap<TIntDoubleHashMap> weights) {
    // get avg
    double totalWeight = 0.0;
    int totalCount = 0;
    for (int e : weights.keys()) {  
      TIntDoubleHashMap kp2mi = weights.get(e);
      for (int kp : kp2mi.keys()) {
        double mi = kp2mi.get(kp);
        totalWeight += mi;
        totalCount++;
      }
    }
    double avg = totalWeight / (double) totalCount;
    
    // divide all values by avg
    TIntObjectHashMap<TIntDoubleHashMap> normEntity2kp2mi = new TIntObjectHashMap<TIntDoubleHashMap>(weights.size(), 1.0f);
    for (int e : weights.keys()) {     
      TIntDoubleHashMap kp2mi = weights.get(e);
      TIntDoubleHashMap normKp2mi = new TIntDoubleHashMap(kp2mi.size(), 1.0f);
      for (int kp : kp2mi.keys()) {
        double unnormMI = kp2mi.get(kp);
        double mi = unnormMI / avg;
        normKp2mi.put(kp, mi);
      }      
      normEntity2kp2mi.put(e, normKp2mi);
    }    
    
    return normEntity2kp2mi;
  }

  private TIntDoubleHashMap averageIDFweights(TIntDoubleHashMap weights) {
    // get avg
    double totalWeight = 0.0;
    int totalCount = 0;
    for (int kp : weights.keys()) {
      double idf = weights.get(kp);
      totalWeight += idf;
      totalCount++;
    }
    double avg = totalWeight / (double) totalCount;    
    
    // divide all values by avg
    TIntDoubleHashMap normKp2idf = new TIntDoubleHashMap(weights.size(), 1.0f);
    for (int kp : weights.keys()) {
      double unnormIDF = weights.get(kp);
      double normIDF = unnormIDF/avg;
      normKp2idf.put(kp, normIDF);
    }
    
    return normKp2idf;
  }

  public int[] getEntityKeyphraseIds(Entity e) {
    return eKps.get(e.getId());
  }
  
  private void calcAndAddIdfWeights( 
      final TIntIntHashMap superDocKeyphraseCounts, 
      final int collectionSize,
      final boolean shouldNormalize,
      TIntDoubleHashMap weights) {    
    for (int kpId : superDocKeyphraseCounts.keys()) {
      if (weights.contains(kpId)) {
        continue; // first idf value computed is highest priority
      }
      
      if (kpId == superDocKeyphraseCounts.getNoEntryKey() && !weights.contains(kpId)) {
        weights.put(kpId, 0.0); // no score
        continue;
      }

      int count = superDocKeyphraseCounts.get(kpId);
      double idf = WeightComputation.log2((double)collectionSize / (double)count);
      
      if (shouldNormalize) {
        idf = idf / WeightComputation.log2(collectionSize); // max idf value is when occurrence count is 1
      }
      
      if (!weights.contains(kpId)) {
        weights.put(kpId, idf);
      }
    }
  }
  
  @SuppressWarnings("unused")
  /**
   * Computes the MI weights for all entity keyphrase pairs.
   * The method is not used anymor as the new database schema has the scores
   * precomputed. Stays here for reference and potential experimental use.
   */
  private void calculateAndAddMIWeights(
      final Entities entities, 
      final TIntIntHashMap entityOccurrenceCounts,
      final TIntIntHashMap keyphraseOccurrenceCounts, 
      TIntObjectHashMap<TIntIntHashMap> entityKeyphraseIntersectionCounts,
      final boolean shouldNormalize,
      int totalCount,
      TIntObjectHashMap<TIntDoubleHashMap> weights) {       
        
    // calcuate MI for the entities
    
    // -----------
    // doc/entity and keyphrases occurrence counts are passed
    // -----------    
    
    // -----------
    // for the intersection
    // -----------       

    // calc all MI scores
    logger.debug("Calculating MI per keyphrase");
    long start = System.currentTimeMillis();
    for (Entity e : entities) {
      for (int kp : entityKeyphraseIntersectionCounts.get(e.getId()).keys()) {        
        int docOccurrenceCount = entityOccurrenceCounts.get(e.getId());
        int entityKeyphraseIntersectionCount = 0; // default if nothing is stored
        if (entityKeyphraseIntersectionCounts.containsKey(e.getId())) {
          TIntIntHashMap keyphraseIntersectionCounts = entityKeyphraseIntersectionCounts.get(e.getId());
          if (keyphraseIntersectionCounts.containsKey(kp)) {
            entityKeyphraseIntersectionCount = keyphraseIntersectionCounts.get(kp);
          }
        }
        int keyphraseOccurrenceCount = keyphraseOccurrenceCounts.containsKey(kp) && (keyphraseOccurrenceCounts.get(kp) != keyphraseOccurrenceCounts.getNoEntryValue()) ? keyphraseOccurrenceCounts.get(kp) : 0;
        
        if (e.getName().equals("$_sign---NME--") && getKeyphraseForId(kp).equals("final note")) {
          System.out.println("here");
        }
        
        double score = 0.0;
        if (!(entityKeyphraseIntersectionCount == 0)) { // no interesection means 0 score
           score = 
               calculateMI(
                   docOccurrenceCount, keyphraseOccurrenceCount, 
                   entityKeyphraseIntersectionCount, totalCount, 
                   shouldNormalize);  
        }

        TIntDoubleHashMap keyphraseMIWeights = weights.get(e.getId());
        if (keyphraseMIWeights == null) {
          keyphraseMIWeights = new TIntDoubleHashMap();
          weights.put(e.getId(), keyphraseMIWeights);
        }
        
        keyphraseMIWeights.put(kp, score);
        
        if (score > 1.0) {
          System.out.println(e.getName() + "\t" + getKeyphraseForId(kp) + "\t" + score);
        }
      }
    }
        
    long runTime = System.currentTimeMillis() - start;
    logger.debug("Calculating MI per keyphrase done (" + runTime/1000 + "s)");
  }

  @SuppressWarnings("unused")
  /**
   * Used be the MI computation, which is disabled for now (precomputed).
   * 
   */
  private int getTotalDocumentCount(Map<String, List<String>> entitySuperDocs, Map<String, List<String>> keyphraseEntities) {
    // MI weights should take into consideration confusability, set total number of document to
    // the number of documents which either contains a keyphrase or an entity
    Set<String> documents = new HashSet<String>();

    // create set of all documents
    // 1. all entities + superdocs
    for (Entry<String, List<String>> e : entitySuperDocs.entrySet()) {
      documents.add(e.getKey());
      for (String s : e.getValue()) {
        documents.add(s);
      }
    }

    // 2. all keyphrase docs
    for (List<String> entities : keyphraseEntities.values()) {
      for (String e : entities) {
        documents.add(e);
      }
    }

    return documents.size();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}