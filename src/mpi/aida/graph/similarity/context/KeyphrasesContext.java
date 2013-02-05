package mpi.aida.graph.similarity.context;

import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Collection;

import mpi.aida.access.DataAccess;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.data.Keyphrases;
import mpi.aida.graph.similarity.measure.WeightComputation;
import mpi.aida.util.RunningTimer;
import mpi.aida.util.StopWord;
import mpi.aida.util.YagoUtil;
import mpi.database.DBConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main class for keyphrases used as context.
 * Keyphrases themselves are not weighted, just the keywords (as in the original AIDA EMNLP 2011 paper)
 * If keyphrase weights are necessary, use the @see WeightedKeyphrasesContext instead. 
 *
 */
public class KeyphrasesContext extends EntitiesContext {
  private static final Logger logger = 
      LoggerFactory.getLogger(KeyphrasesContext.class);
  
  protected TIntObjectHashMap<int[]> eKps;
  protected TIntObjectHashMap<int[]> kpTokens;
  protected TIntObjectHashMap<int[]> kpTokensNoStopwords;
  protected TIntHashSet allKeyphrases;
  protected TIntHashSet allKeywords;
  
  protected TIntObjectHashMap<TIntDoubleHashMap> entity2keyword2mi; 
  protected TIntObjectHashMap<TIntDoubleHashMap> entity2keyphrase2mi;
  protected TIntDoubleHashMap keyword2idf;
    
  protected Entities realEntities;
    
  DBConnection con;

  public KeyphrasesContext(Entities entities) throws Exception {
    super(entities, null);
  }
  
  public KeyphrasesContext(Entities entities, EntitiesContextSettings settings) throws Exception {
    super(entities, settings);
  }

  @Override
  public int[] getContext(Entity entity) {
    return eKps.get(entity.getId());
  }

  public int[] getKeyphraseTokens(int keyphrase) {
    return kpTokens.get(keyphrase);
  }

  public double getKeywordIDFWeight(int keyword) {
    return keyword2idf.get(keyword);
  }

  public double getKeywordMIWeight(Entity entity, int keyword) {
    return entity2keyword2mi.get(entity.getId()).get(keyword);
  }
  
  public String getKeyphraseForId(int keyphraseId) {
    return DataAccess.getWordForId(keyphraseId);
  }
  
  public int getIdForKeyphrase(String keyphrase) {
    return DataAccess.getIdForWord(keyphrase);
  }
  
  public String getKeywordForId(int keywordId) {
    return DataAccess.getWordForId(keywordId);
  }
  
  public int getIdForKeyword(String keyword) {
    return DataAccess.getIdForWord(keyword);
  }

  @Override
  protected void setupEntities(Entities entities) throws Exception {   
    // initialize all datastructures
    keyword2idf = new TIntDoubleHashMap();
    
    // remove nme entities before setting up
    realEntities = new Entities();
    for (Entity e : entities) {
      if (!e.isNMEentity()) {
        realEntities.add(e);
      }
    }

    String keyphraseSourceExclusion = null;
    if (settings != null && settings.getKeyphraseSourceExclusion() != null) {
      keyphraseSourceExclusion = settings.getKeyphraseSourceExclusion();
    }

    logger.debug("Retrieving all entity keyphrases/keywords + weights");
    Integer uniqueId = RunningTimer.start(getIdentifier());
    RunningTimer.stageStart(
        getIdentifier(), "EntityKeyphrasesTokensMI", uniqueId);
    Keyphrases keyphrases = 
        DataAccess.getEntityKeyphrases(entities, keyphraseSourceExclusion);
    eKps = keyphrases.getEntityKeyphrases();
    kpTokens = keyphrases.getKeyphraseTokens();
    entity2keyword2mi = keyphrases.getEntityKeywordWeights();
    entity2keyphrase2mi = keyphrases.getEntityKeyphraseWeights();
    RunningTimer.stageEnd(
        getIdentifier(), "EntityKeyphrasesTokensMI", uniqueId);
    
    // Store all keywords and keywords without stopwords.
    allKeyphrases = new TIntHashSet();
    allKeywords = new TIntHashSet();
    kpTokensNoStopwords = new TIntObjectHashMap<int[]>();

    for (int keyphrase : kpTokens.keys()) {
      allKeyphrases.add(keyphrase);
      int[] keywords = kpTokens.get(keyphrase); 
      allKeywords.addAll(keywords);
      TIntLinkedList keywordsNoStopwords = new TIntLinkedList();
      for (int keyword : keywords) {
        if (!StopWord.is(keyword)) {
          keywordsNoStopwords.add(keyword);
        }
      }
      kpTokensNoStopwords.put(keyphrase, keywordsNoStopwords.toArray());
    }
    
    logger.debug("Retrieving counts");
    RunningTimer.stageStart(
        getIdentifier(), "KeywordCounts", uniqueId);
    TIntIntHashMap keywordDF = DataAccess.getKeywordDocumentFrequencies(allKeywords);
    RunningTimer.stageEnd(
        getIdentifier(), "KeywordCounts", uniqueId);
    
    logger.debug("Computing all keyword IDF weights");    
    RunningTimer.stageStart(
        getIdentifier(), "IDFComputation", uniqueId);
    computeIDFweights(keywordDF); 
    RunningTimer.stageEnd(
        getIdentifier(), "IDFComputation", uniqueId);
    RunningTimer.end(getIdentifier(), uniqueId);
    
    logger.debug("Finished KeyphrasesContext for " +
        entities.uniqueNameSize() + " entities, " +
        allKeywords.size() + " keywords");
  }

  @SuppressWarnings("unused")
  private void computeMIweights(
      Collection<Integer> collection, TIntIntHashMap keywordDF, TIntIntHashMap entitySDS,
      TIntObjectHashMap<TIntIntHashMap> entityKeywordIC) {
    for (int e : collection) {      
      TIntDoubleHashMap keyword2mi = new TIntDoubleHashMap();
      entity2keyword2mi.put(e, keyword2mi);
      
      int[] keyphrases = eKps.get(e);
      if (keyphrases == null) {
        continue;
      }

      // store all tokens + ids, get all mi weights
      for (int keyphrase : keyphrases) {      
        for (int keyword : kpTokens.get(keyphrase)) {                                         
          if (keyword2mi.containsKey(keyword)) {
            continue;
          }
          
          int entityOccurrenceCount = entitySDS.get(e);
          int keywordOccurrenceCount = keywordDF.get(keyword);
          int intersectionCount = entityKeywordIC.get(e).get(keyword);
        
          boolean shouldNormalize = (settings != null) ? settings.shouldNormalizeWeights() : false;
          double miWeight = 
              calculateMI(
                  entityOccurrenceCount, keywordOccurrenceCount, 
                  intersectionCount, getNSize(), shouldNormalize);
        
          if (Double.isNaN(miWeight)) {
            System.out.println(
                "Keyword borked, setting to 0.0 weight: " + 
                getKeywordForId(keyword));
            miWeight = 0.0;
          }
                  
          keyword2mi.put(keyword, miWeight);
        }
      }
    }
  }
  
  private void computeIDFweights(TIntIntHashMap keywordDF) {
    for (int keyword : keywordDF.keys()) {
      if (keyword2idf.contains(keyword)) {
        continue; // first idf value computed is highest priority
      }
      
      int df = keywordDF.get(keyword);         
      double idf = WeightComputation.log2(getNSize() / df);
      
      if (Double.isNaN(idf)) {
        logger.debug("Keyword IDF '" +
                  getKeywordForId(keyword) + 
                  "' is borked, setting to 0.0");
        idf = 0.0;
      }
      
      boolean shouldNormalize = (settings != null) ? settings.shouldNormalizeWeights() : false;
      if (shouldNormalize) {
        idf = idf / WeightComputation.log2(getNSize());
      }
           
      keyword2idf.put(keyword, idf);
    }
  }

  protected int getNSize() {
    return YagoUtil.TOTAL_YAGO_ENTITIES;
  }
  
  protected double calculateMI(
      int aOcc, int bOcc, int abOcc, int totalOcc)
  {
        return WeightComputation.computeMI(
            aOcc, bOcc, abOcc, totalOcc, false);
  }
  
  protected double calculateMI(
      int aOcc, int bOcc, int abOcc, int totalOcc, boolean normalize) {
    return WeightComputation.computeMI(
        aOcc, bOcc, abOcc, totalOcc, normalize);
  }
}