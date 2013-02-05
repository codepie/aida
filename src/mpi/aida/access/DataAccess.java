package mpi.aida.access;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mpi.aida.config.AidaConfig;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.data.Keyphrases;
import mpi.aida.util.YagoUtil.Gender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAccess {
  private static final Logger logger = 
      LoggerFactory.getLogger(DataAccess.class);

  private static DataAccessInterface dataAccess = null;

  /* Keyphrase sources */
  public static final String KPSOURCE_LINKANCHOR = "linkAnchor";

  public static final String KPSOURCE_INLINKTITLE = "inlinkTitle";

  public static final String KPSOURCE_CATEGORY = "wikipediaCategory";

  public static final String KPSOURCE_CITATION = "citationTitle";

  /** which type of data access*/
  public static enum type {
    sql, testing
  };

  private static synchronized void initDataAccess() {
    if (dataAccess != null) {
      return;
    }
    if (DataAccess.type.sql.toString().equalsIgnoreCase(AidaConfig.get(AidaConfig.DATAACCESS))) {
      dataAccess = new DataAccessSQL();
    } else if (DataAccess.type.testing.toString().equalsIgnoreCase(AidaConfig.get(AidaConfig.DATAACCESS))) {
      dataAccess = new DataAccessForTesting();
    } else {
      // Default is sql.
      logger.info("No dataAccess given in 'settings/aida.properties', " +
      		        "using 'sql' as default.");
      dataAccess = new DataAccessSQL();
    }
  }

  private static DataAccessInterface getInstance() {
    if (dataAccess == null) {
      initDataAccess();
    }
    return dataAccess;
  }

  public static void init() {
    DataAccess.getInstance();
  }

  public static DataAccess.type getAccessType() {
    return DataAccess.getInstance().getAccessType();
  }

  public static Entities getEntitiesForMention(String mention) {
    return DataAccess.getInstance().getEntitiesForMention(mention);
  }
  
  public static Keyphrases getEntityKeyphrases(Entities entities) {
    return DataAccess.getInstance().getEntityKeyphrases(entities, null);
  }
  
  public static Keyphrases getEntityKeyphrases(
      Entities entities, String keyphrasesSourceExclusion) {
    return DataAccess.getInstance().
        getEntityKeyphrases(entities, keyphrasesSourceExclusion);
  }

  public static void getEntityKeyphraseTokens(
      Entities entities,
      TIntObjectHashMap<int[]> entityKeyphrases,
      TIntObjectHashMap<int[]> keyphraseTokens) {
    getEntityKeyphraseTokens(entities, null, entityKeyphrases, keyphraseTokens);
  }
  
  public static void getEntityKeyphraseTokens(
      Entities entities, String keyphraseSourceExclusion,
      TIntObjectHashMap<int[]> entityKeyphrases,
      TIntObjectHashMap<int[]> kpTokens) {
    DataAccess.getInstance().getEntityKeyphraseTokens(
        entities, keyphraseSourceExclusion, entityKeyphrases, kpTokens);
  }

  public static int[] getInlinkNeighbors(Entity e) {
    return DataAccess.getInstance().getInlinkNeighbors(e);
  }

  public static TIntObjectHashMap<int[]> getInlinkNeighbors(Entities entities) {
    return DataAccess.getInstance().getInlinkNeighbors(entities);
  }

  public static int getIdForYagoEntityId(String entity) {
    List<String> entities = new ArrayList<String>(1);
    entities.add(entity);
    return getIdsForYagoEntityIds(entities).get(entity);
  }
  
  public static String getYagoEntityIdForId(int entity) {
    int[] entities = new int[1];
    entities[0] = entity;
    return getYagoEntityIdsForIds(entities).get(entity);
  }

  public static TIntObjectHashMap<String> getYagoEntityIdsForIds(int[] ids) {
    return DataAccess.getInstance().getYagoEntityIdsForIds(ids);
  }
  
  public static TObjectIntHashMap<String> 
  getIdsForYagoEntityIds(Collection<String> entityIds) {
    return DataAccess.getInstance().getIdsForYagoEntityIds(entityIds);
  }

  public static String getWordForId(int wordId) {
    int[] words = new int[1];
    words[0] = wordId;
    return getWordsForIds(words).get(wordId);
  }

  public static int getIdForWord(String word) {
    List<String> words = new ArrayList<String>(1);
    words.add(word);
    return getIdsForWords(words).get(word);  
  }
  
  public static TIntObjectHashMap<String> getWordsForIds(int[] wordIds) {
    return DataAccess.getInstance().getWordsForIds(wordIds);
  }
  
  /**
   * Transform words into the token ids. Unknown words will be assigned
   * the id -1
   * 
   * @param   words
   * @return  Ids for words ranging from 1 to MAX, -1 for missing.
   */
  public static TObjectIntHashMap<String> getIdsForWords(
      Collection<String> words) {
    return DataAccess.getInstance().getIdsForWords(words);
  }

  public static Map<String, Gender> getGenderForEntities(Entities entities) {
    return getInstance().getGenderForEntities(entities);
  }

  public static Map<String, List<String>> getTypes(Set<String> entities) {
    return getInstance().getTypes(entities);
  }

  public static List<String> getTypes(String Entity) {
    return getInstance().getTypes(Entity);
  }

  public static TIntIntHashMap getKeyphraseDocumentFrequencies(TIntHashSet keyphrases) {
    return getInstance().getKeyphraseDocumentFrequencies(keyphrases);
  }

  public static List<String> getParentTypes(String queryType) {
    return getInstance().getParentTypes(queryType);
  }

  public static boolean checkEntityNameExists(String entityName) {
    return getInstance().checkEntityNameExists(entityName);
  }

  public static String getKeyphraseSource(String entityName, String keyphrase) {
    return getInstance().getKeyphraseSource(entityName, keyphrase);
  }

  public static Map<Entity, int[]> getEntityLSHSignatures(Entities entities) {
    return getInstance().getEntityLSHSignatures(entities);
  }

  public static String getFamilyName(String entity) {
    return getInstance().getFamilyName(entity);
  }

  public static String getGivenName(String entity) {
    return getInstance().getGivenName(entity);
  }

  public static Map<Entity, int[]> getEntityLSHSignatures(Entities entities, String table) {
    return getInstance().getEntityLSHSignatures(entities, table);
  }

  public static TIntDoubleHashMap getEntityPriors(String mention) {
    return getInstance().getEntityPriors(mention);
  }

  public static TIntIntHashMap getKeywordDocumentFrequencies(TIntHashSet keywords) {
    return getInstance().getKeywordDocumentFrequencies(keywords);
  }

  public static TIntIntHashMap getEntitySuperdocSize(Entities entities) {
    return getInstance().getEntitySuperdocSize(entities);
  }

  public static TIntObjectHashMap<TIntIntHashMap> getEntityKeywordIntersectionCount(Entities entities) {
    return getInstance().getEntityKeywordIntersectionCount(entities);
  }

  public static TObjectIntHashMap<String> getAllEntityIds() {
    return getInstance().getAllEntityIds();
  }
  
  public static Entities getAllEntities() {
    return getInstance().getAllEntities();
  }

  // Will return an array where the index is the id of the word to be expanded
  // and the value is the id of the expanded word. Expansion is done by
  // fully uppercasing the original word behind the id.
  public static int[] getAllWordExpansions() {
    return getInstance().getAllWordExpansions();
  }

  public static boolean isYagoEntity(Entity entity) {
    return getInstance().isYagoEntity(entity);
  }
}
