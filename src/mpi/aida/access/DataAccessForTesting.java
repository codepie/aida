package mpi.aida.access;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mpi.aida.AidaManager;
import mpi.aida.access.DataAccess.type;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.data.Keyphrases;
import mpi.aida.graph.similarity.measure.WeightComputation;
import mpi.aida.util.YagoUtil.Gender;

public class DataAccessForTesting implements DataAccessInterface {

  private Map<String, Integer> entity2id = new HashMap<String, Integer>();
  private Map<Integer, String> id2entity = new HashMap<Integer, String>();
  
  private Map<String, Integer> word2id = new HashMap<String, Integer>();
  private Map<Integer, String> id2word = new HashMap<Integer, String>();
  
  private TIntIntHashMap wordExpansions = new TIntIntHashMap();
  
  private int entityId = 0;
  private int wordId = 0;
  
  private final int TOTAL_ENTITY_COUNT = 2651987;
      
  /**
   * All entities with keyphrases and count. Format is:
   * entity, kp1, count1, kp2, count2, ...
   * kpN is space separated tokens.
   */
  private String[][] allEntityKeyphrases = new String[][] {
      new String[] { "Larry_Page", "Google", "2" },
      new String[] { "Jimmy_Page", "played", "10", "Les Paul", "4", "tuned", "1", "Led Zeppelin", "5", "Robert Plant", "9", "Rock music", "2"},
      new String[] { "Nomatching_Page", "Page", "5" },
      new String[] { "Stopword_Page", "and the", "2" },
      new String[] { "Kashmir", "China", "10" },
      new String[] { "Kashmir_(song)", "Jimmy Page", "5", "festival", "2", "Led Zeppelin", "3", "Robert Plant", "5" },
      new String[] { "Knebworth_Festival", "festival", "1", "Rock music", "2" },
  };
  
  /**
   * All entity superdoc sizes. Format is:
   * entity, size
   */
  private String[][] allEntitySizes = new String[][] {
      new String[] { "Larry_Page", "20" },
      new String[] { "Jimmy_Page", "10" },
      new String[] { "Nomatching_Page", "5" },
      new String[] { "Stopword_Page", "2" },
      new String[] { "Kashmir", "15" },
      new String[] { "Kashmir_(song)", "5" },
      new String[] { "Knebworth_Festival", "2" },
  };
  
  /**
   * All keyphrase superdoc frequencies. Format is:
   * keyphrase, frequency
   */
  private String[][] allKeyphraseFrequencies = new String[][] {
      new String[] { "Google", "50" },
      new String[] { "played", "100" },
      new String[] { "Les Paul", "80" },
      new String[] { "tuned", "20" },
      new String[] { "China", "200" },
      new String[] { "Jimmy Page", "30" },
      new String[] { "festival", "10" },
      new String[] { "Led Zeppelin", "40" },
      new String[] { "Robert Plant", "25" },
      new String[] { "Rock music", "30" },
      new String[] { "and the", "5" },
  };
  
  /** All entity inlinks */
  private String[][] allInlinks = new String[][] {
      new String[] { "Larry_Page", "Google" },
      new String[] { "Jimmy_Page", "Led_Zeppelin", "Robert_Plant", "Rock", "Les_Paul" },
      new String[] { "Kashmir", "China", "India", "Pakistan" },
      new String[] { "Kashmir_(song)", "Led_Zeppelin", "Robert_Plant", "Jimmy_Page" },
      new String[] { "Knebworth_Festival", "England", "Music_Festival", "Led_Zeppelin" },
  };
   
  @Override
  public int[] getInlinkNeighbors(Entity entity) {
    Entities singleEntity = new Entities();
    singleEntity.add(entity);
    return getInlinkNeighbors(singleEntity).get(entity.getId());
  }
      
      
  @Override
  public void getEntityKeyphraseTokens(
      Entities entities, String keyphraseSourceExclusion,
      TIntObjectHashMap<int[]> entityKeyphrases,
      TIntObjectHashMap<int[]> keyphraseTokens) {
    for (String[] eKps : allEntityKeyphrases) {
      int entity = DataAccess.getIdForYagoEntityId(eKps[0]);
      int[] keyphrases = new int[(eKps.length - 1) / 2];
      if (eKps.length > 1) {
        for (int i = 1; i < eKps.length; ++i) {
          if (i % 2 == 1) {
            int kp = DataAccess.getIdForWord(eKps[i]);
            keyphrases[(i - 1) / 2] = kp;
            
            // Add tokens.
            String[] tokens = eKps[i].split(" ");
            int[] tokenIds = new int[tokens.length];
            for (int j = 0; j < tokens.length; ++j) {
              tokenIds[j] = DataAccess.getIdForWord(tokens[j]);
            }
            keyphraseTokens.put(kp, tokenIds);
          }
        }
      }
      entityKeyphrases.put(entity, keyphrases);
    }
  }

  public TIntObjectHashMap<TIntIntHashMap> getEntityKeyphraseIntersectionCount(
      Entities entities) {
    TIntObjectHashMap<TIntIntHashMap> isec = new TIntObjectHashMap<TIntIntHashMap>();
    for (String[] eKps : allEntityKeyphrases) {
      int entity = DataAccess.getIdForYagoEntityId(eKps[0]);
      TIntIntHashMap counts = new TIntIntHashMap();
      isec.put(entity, counts);
      
      if (eKps.length > 1) {
        int currentKp = -1;
        for (int i = 1; i < eKps.length; ++i) {
          if (i % 2 == 1) {
            currentKp = DataAccess.getIdForWord(eKps[i]);
          } else {
            int count = Integer.parseInt(eKps[i]);
            counts.put(currentKp, count);
          }
        }
      }
    }
    return isec;
  }

  public DataAccessForTesting() {
    for (String[] entity : allEntityKeyphrases) {
      addEntity(entity[0]);
    }

    for (String[] inlinks : allInlinks) {
      for (int i = 1; i < inlinks.length; ++i) {
        addEntity(inlinks[i]);
      }
    }
    
    for (Entry<String, Integer> e : entity2id.entrySet()) {
      id2entity.put(e.getValue(), e.getKey());
    }
        
    for (String[] entity : allEntityKeyphrases) {
      for (int i = 1; i < entity.length; ++i) {
        if (i % 2 == 1) {     
          addWord(entity[i]);
          
          String[] tokens = entity[i].split(" ");
          for (String token : tokens) {
            addWord(token);
          }
        }
      }
    }

    for (Entry<String, Integer> e : word2id.entrySet()) {
      id2word.put(e.getValue(), e.getKey());
    }
  }
  
  private void addEntity(String name) {
    entity2id.put(name, entityId++);
  }
  
  private void addWord(String word) {
    // Don't add twice.=
    if (word2id.containsKey(word)) {
      return;
    }
    
    int id = wordId;
    ++wordId;
    word2id.put(word, id);
    String wordUpper = AidaManager.expandTerm(word);
    int upper = wordId;
    if (word2id.containsKey(wordUpper)) {
      upper = word2id.get(wordUpper);
    } else {
      word2id.put(wordUpper, upper);
      ++wordId;
    }
    wordExpansions.put(id, upper);
  }
  
  @Override
  public type getAccessType() {
    return DataAccess.type.testing;
  }

  @Override
  public Entities getEntitiesForMention(String mention) {
    if (mention.equals("Page")) {
      Entities pageEntities = new Entities();
      pageEntities.add(new Entity("Jimmy_Page", DataAccess.getIdForYagoEntityId("Jimmy_Page")));
      pageEntities.add(new Entity("Larry_Page", DataAccess.getIdForYagoEntityId("Larry_Page")));
      return pageEntities;
    } else if (mention.equals("Kashmir")) {
      Entities kashmirEntities = new Entities();
      kashmirEntities.add(new Entity("Kashmir", DataAccess.getIdForYagoEntityId("Kashmir")));
      kashmirEntities.add(new Entity("Kashmir_(song)", DataAccess.getIdForYagoEntityId("Kashmir_(song)")));
      return kashmirEntities;
    } else if (mention.equals("Knebworth")) {
      Entities knebworthEntities = new Entities();
      knebworthEntities.add(new Entity("Knebworth_Festival", DataAccess.getIdForYagoEntityId("Knebworth_Festival")));
      return knebworthEntities;
    } else if (mention.equals("Les Paul")) {
      return new Entities();
    } else {
      throw new IllegalArgumentException(mention + " is not part of Testing");
    }
  }

  @Override
  public Keyphrases getEntityKeyphrases(Entities entities,
      String keyphraseSourceExclusion) {
    Keyphrases keyphrases = new Keyphrases();
    TIntObjectHashMap<int[]> eKps = new TIntObjectHashMap<int[]>();
    TIntObjectHashMap<int[]> kpTokens = new TIntObjectHashMap<int[]>();
    getEntityKeyphraseTokens(
        entities, keyphraseSourceExclusion, eKps, kpTokens);
    keyphrases.setEntityKeyphrases(eKps);
    keyphrases.setKeyphraseTokens(kpTokens);
    
    // TODO(jhoffart) add keyphrase mi weights.
    TIntObjectHashMap<TIntDoubleHashMap> e2kw2mi = 
        new TIntObjectHashMap<TIntDoubleHashMap>();
    keyphrases.setEntityKeywordWeights(e2kw2mi);
    TIntObjectHashMap<TIntDoubleHashMap> e2kp2mi = 
        new TIntObjectHashMap<TIntDoubleHashMap>();
    keyphrases.setEntityKeyphraseWeights(e2kp2mi);    
  
    for (Entity entity : entities) {
      int eId = entity.getId();
      Entities singleEntity = new Entities();
      singleEntity.add(entity);
      int entityCount = getEntitySuperdocSize(singleEntity).get(eId);
      TIntDoubleHashMap kp2mi = new TIntDoubleHashMap();
      e2kp2mi.put(entity.getId(), kp2mi);
      TIntDoubleHashMap kw2mi = new TIntDoubleHashMap();
      e2kw2mi.put(entity.getId(), kw2mi);
      for (int kp : eKps.get(eId)) {
        TIntHashSet singleKp = new TIntHashSet();
        singleKp.add(kp);
        int kpCount = getKeyphraseDocumentFrequencies(singleKp).get(kp);
        int eKpIcCount = 
            getEntityKeyphraseIntersectionCount(singleEntity).get(eId).get(kp);
        kp2mi.put(kp, 
            WeightComputation.computeNPMI(
                entityCount, kpCount, eKpIcCount, 
                TOTAL_ENTITY_COUNT));
        
        for (int kw : kpTokens.get(kp)) {
          TIntHashSet singleKw = new TIntHashSet();
          singleKw.add(kw);
          int kwCount = getKeywordDocumentFrequencies(singleKw).get(kw);
          int eKwIcCount = 
              getEntityKeywordIntersectionCount(singleEntity).get(eId).get(kw);
          kw2mi.put(kw, 
              WeightComputation.computeMI(
                  entityCount, kwCount, eKwIcCount, 
                  TOTAL_ENTITY_COUNT, false));
        }
      }
    }
    return keyphrases;
  }

  @Override
  public TIntObjectHashMap<int[]> getInlinkNeighbors(Entities entities) {
    TIntObjectHashMap<int[]> inlinks = new TIntObjectHashMap<int[]>();
    
    for (Entity e : entities) {
      inlinks.put(e.getId(), new int[0]);
    }
    
    for (String[] entityInlinks : allInlinks) {
      int eId = DataAccess.getIdForYagoEntityId(entityInlinks[0]);
      int[] inlinkIds = new int[entityInlinks.length - 1];
      for (int i = 1; i < entityInlinks.length; ++i) {
        inlinkIds[i-1] = DataAccess.getIdForYagoEntityId(entityInlinks[i]);
      }
      Arrays.sort(inlinkIds);
      inlinks.put(eId, inlinkIds);
    }
    
    return inlinks;
  }

  @Override
  public TObjectIntHashMap<String> getIdsForYagoEntityIds(
      Collection<String> entities) {
    TObjectIntHashMap<String> ids = 
        new TObjectIntHashMap<String>(entities.size());
    for (String entity : entities) {
      if (!entity2id.containsKey(entity)) {
        throw new IllegalArgumentException(entity + " not in testing");
      } else {
        ids.put(entity, entity2id.get(entity));
      }
    }
    return ids;
  }

  @Override
  public TIntObjectHashMap<String> getYagoEntityIdsForIds(int[] ids) {
    TIntObjectHashMap<String> entities = new TIntObjectHashMap<String>();
    for (int i = 0; i < ids.length; ++i) {
      if (!id2entity.containsKey(ids[i])) {
        throw new IllegalArgumentException(ids[i] + " not in testing");
      } else {
        entities.put(ids[i], id2entity.get(ids[i]));
      }
      ++i;
    }
    return entities;
  }

  @Override
  public TIntObjectHashMap<String> getWordsForIds(int[] ids) {
    TIntObjectHashMap<String> words = new TIntObjectHashMap<String>();
    for (int i = 0; i < ids.length; ++i) {
      if (!id2word.containsKey(ids[i])) {
        throw new IllegalArgumentException(ids[i] + " not in testing");
      } else {
        words.put(ids[i], id2word.get(ids[i]));
      }
    }
    return words;
  }

  @Override
  public TObjectIntHashMap<String> getIdsForWords(Collection<String> words) {
    TObjectIntHashMap<String> ids = new TObjectIntHashMap<String>(words.size());
    for (String word : words) {
      // The default id for an unknown word is -1
      int id = -1; 
      if (word2id.containsKey(word)) {
        id = word2id.get(word);
      }
      ids.put(word, id);
    }
    return ids;
  }
  
  @Override
  public Map<String, Gender> getGenderForEntities(Entities entities) {
    System.err.println("Accessed " + getMethodName());
    return null;
  }

  @Override
  public Map<String, List<String>> getTypes(Set<String> entities) {
    System.err.println("Accessed " + getMethodName());
    return null;
  }

  @Override
  public List<String> getTypes(String Entity) {
    System.err.println("Accessed " + getMethodName());
    return null;
  }

  @Override
  public List<String> getParentTypes(String queryType) {
    System.err.println("Accessed " + getMethodName());
    return null;
  }

  @Override
  public boolean checkEntityNameExists(String entity) {
    System.err.println("Accessed " + getMethodName());
    return false;
  }

  @Override
  public String getKeyphraseSource(String entityName, String keyphrase) {
    System.err.println("Accessed " + getMethodName());
    return null;
  }

  @Override
  public Map<String, List<String>> getEntityKeyphrases(Set<String> entities) {
    System.err.println("Accessed " + getMethodName());
    return null;
  }

  @Override
  public Map<String, List<String>> getKeyphraseEntities(Set<String> keyphrases) {
    System.err.println("Accessed " + getMethodName());
    return null;
  }

  @Override
  public Map<Entity, int[]> getEntityLSHSignatures(Entities entities) {
    System.err.println("Accessed " + getMethodName());
    return null;
  }

  @Override
  public String getFamilyName(String entity) {
    System.err.println("Accessed " + getMethodName());
    return null;
  }

  @Override
  public String getGivenName(String entity) {
    System.err.println("Accessed " + getMethodName());
    return null;
  }

  @Override
  public Map<Entity, int[]> getEntityLSHSignatures(Entities entities, String table) {
    System.err.println("Accessed " + getMethodName());
    return null;
  }

  @Override
  public TIntDoubleHashMap getEntityPriors(String mention) {    
    if (mention.equals("PAGE")) {
      TIntDoubleHashMap pagePriors = 
          new TIntDoubleHashMap();
      pagePriors.put(DataAccess.getIdForYagoEntityId("Jimmy_Page"), 0.3);
      pagePriors.put(DataAccess.getIdForYagoEntityId("Larry_Page"), 0.7);
      return pagePriors;
    } else if (mention.equals("KASHMIR")) {
      TIntDoubleHashMap kashmirPriors =
          new TIntDoubleHashMap();
      kashmirPriors.put(DataAccess.getIdForYagoEntityId("Kashmir"), 0.9);
      kashmirPriors.put(DataAccess.getIdForYagoEntityId("Kashmir_(song)"), 0.1);
      return kashmirPriors;
    } else if (mention.equals("KNEBWORTH")) {
      TIntDoubleHashMap knebworthPriors = 
          new TIntDoubleHashMap();
      knebworthPriors.put(DataAccess.getIdForYagoEntityId("Knebworth_Festival"), 1.0);
      return knebworthPriors;
    } else if (mention.equals("LES PAUL")) {
      return new TIntDoubleHashMap();
    } else {
      throw new IllegalArgumentException(mention + " is not part of Testing");
    }
  }

  @Override
  public TIntIntHashMap getKeywordDocumentFrequencies(TIntHashSet keywords) {
    TIntIntHashMap freqs = new TIntIntHashMap();
    for (String[] kpF : allKeyphraseFrequencies) {
      String[] tokens = kpF[0].split(" ");
      int freq = Integer.parseInt(kpF[1]);
      for (String token : tokens) {
        freqs.put(DataAccess.getIdForWord(token), freq);
      }
    }
    
    for (int kw : keywords.toArray()) {
      if (!freqs.containsKey(kw)) {
        System.err.println(
            "allKeyphraseFrequencies do not contain token '" + 
            DataAccess.getWordForId(kw) + "'");
      }     
    }
    
    return freqs;
  }

  @Override
  public TIntIntHashMap getEntitySuperdocSize(Entities entities) {
    TIntIntHashMap sizes = new TIntIntHashMap();
    for (String entity[] : allEntitySizes) {
      int id = DataAccess.getIdForYagoEntityId(entity[0]);
      int size = Integer.parseInt(entity[1]);
      sizes.put(id, size);
    }

    for (Entity e : entities) {
      if (!sizes.containsKey(e.getId())) {
        System.err.println(
            "allEntitySizes does not contain '" + 
            DataAccess.getYagoEntityIdForId(e.getId()) + "'");
      }    
    }
    
    return sizes;
  }

  @Override
  public TIntObjectHashMap<TIntIntHashMap> getEntityKeywordIntersectionCount(Entities entities) {
    TIntObjectHashMap<TIntIntHashMap> isec = 
        new TIntObjectHashMap<TIntIntHashMap>();
    for (String[] eKps : allEntityKeyphrases) {
      int entity = DataAccess.getIdForYagoEntityId(eKps[0]);
      TIntIntHashMap counts = new TIntIntHashMap();
      isec.put(entity, counts);
      
      if (eKps.length > 1) {
        String[] tokens = null;
        for (int i = 1; i < eKps.length; ++i) {
          if (i % 2 == 1) {
            tokens = eKps[i].split(" ");
          } else {
            int count = Integer.parseInt(eKps[i]);
            for (String token : tokens) {
              counts.adjustOrPutValue(DataAccess.getIdForWord(token), count, count); 
            }
          }
        }
      }
    }
    return isec;
  }

  private static String getMethodName()
  {
    StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
    StackTraceElement e = stacktrace[2];
    String methodName = e.getMethodName();
    return methodName;
  }

  @Override
  public TIntIntHashMap getKeyphraseDocumentFrequencies(TIntHashSet keyphrases) {
    TIntIntHashMap freqs = new TIntIntHashMap();
    for (String[] kpF : allKeyphraseFrequencies) {
      int keyphrase = DataAccess.getIdForWord(kpF[0]);
      int freq = Integer.parseInt(kpF[1]);
      freqs.put(keyphrase, freq);
    }
    
    for (int kp : keyphrases.toArray()) {
      if (!freqs.containsKey(kp)) {
        System.err.println(
            "allKeyphraseFrequencies does not contain '" + 
            DataAccess.getWordForId(kp) + "'");
      }
    }
    
    return freqs;
  }

  @Override
  public TObjectIntHashMap<String> getAllEntityIds() {
    System.err.println("Accessed " + getMethodName());
    return null;
  }

  @Override
  public Entities getAllEntities() {
    System.err.println("Accessed " + getMethodName());
    return null;
  }

  @Override
  public int[] getAllWordExpansions() {
    int max = -1;
    for (int i : wordExpansions.keys()) {
      if (i > max) {
        max = i;
      }
    }
    
    int[] expansions = new int[max + 1];
    for (int i : wordExpansions.keys()) {
      expansions[i] = wordExpansions.get(i);
    }
    return expansions;
  }

  @Override
  public boolean isYagoEntity(Entity entity) {
    System.err.println("Accessed " + getMethodName());
    return false;
  }  
}
