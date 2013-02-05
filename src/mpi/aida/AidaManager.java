package mpi.aida;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import mpi.aida.access.DataAccess;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.data.Mention;
import mpi.aida.data.Mentions;
import mpi.aida.data.PreparedInput;
import mpi.aida.data.ResultEntity;
import mpi.aida.preparation.AidaTokenizerManager;
import mpi.aida.preparation.mentionrecognition.FilterMentions;
import mpi.aida.preparation.mentionrecognition.FilterMentions.FilterType;
import mpi.aida.util.YagoUtil.Gender;
import mpi.database.DBConnection;
import mpi.database.DBSettings;
import mpi.database.MultipleDBManager;
import mpi.tokenizer.data.Tokenizer;
import mpi.tokenizer.data.Tokens;
import basics.Normalize;

public class AidaManager {

  // This is more couple to SQL than it should be. Works for now.
  public static final String DB_AIDA = "DatabaseAida";
  public static final String DB_YAGO2_FULL = "DatabaseYago2Full";
  public static final String DB_YAGO2_SPOTLX = "DatabaseYago2SPOTLX";

  private static String databaseAidaConfig = "./settings/database_aida.properties";
  private static String databaseYAGO2FullConfig = "./settings/database_yago2full.properties";
  private static String databaseYAGO2SPOTLXConfig = "./settings/database_yago2spotlx.properties";

  public static final String WIKIPEDIA_PREFIX = "http://en.wikipedia.org/wiki/";
  public static final String YAGO_PREFIX = "http://yago-knowledge.org/resource/";
  
  private static Map<String, String> dbIdToConfig = 
      new HashMap<String, String>();
  
  static {
    dbIdToConfig.put(DB_AIDA, databaseAidaConfig);
    dbIdToConfig.put(DB_YAGO2_FULL, databaseYAGO2FullConfig);   
    dbIdToConfig.put(DB_YAGO2_SPOTLX, databaseYAGO2SPOTLXConfig);
  }
  
  private static AidaManager tasks = null;
  
  private static int[] wordExpansions = null;

  public static enum language {
    english, german
  }

  private static final Set<String> malePronouns = new HashSet<String>() {

    private static final long serialVersionUID = 2L;
    {
      add("He");
      add("he");
      add("Him");
      add("him");
      add("His");
      add("his");
    }
  };

  private static final Set<String> femalePronouns = new HashSet<String>() {

    private static final long serialVersionUID = 3L;
    {
      add("she");
      add("she");
      add("Her");
      add("her");
      add("Hers");
      add("hers");
    }
  };

  public static void init() {
    getTasksInstance();
  }

  private static synchronized AidaManager getTasksInstance() {
    if (tasks == null) {
      tasks = new AidaManager();
    }
    return tasks;
  }

  private static synchronized void initWordExpansion() {
    wordExpansions = DataAccess.getAllWordExpansions();
  }

  public static PreparedInput prepareInputData(String text, String docId, FilterType by) {
    return AidaManager.getTasksInstance().createPrepareInputData(text, docId, by);
  }

  /**
   * tokenizes only the text,
   * 
   * @param docId
   * @param text
   * @return
   */
  public static Tokens tokenize(String docId, String text, boolean lemmatize) {
    return AidaManager.getTasksInstance().tokenize(docId, text, Tokenizer.type.tokens, lemmatize);
  }

  public static Tokens tokenize(String docId, String text) {
    return AidaManager.getTasksInstance().tokenize(docId, text, Tokenizer.type.tokens, false);
  }

  /**
   * tokenizes the text with POS and NER
   * 
   * @param docId
   * @param text
   * @return
   */
  public static Tokens tokenizeNER(String docId, String text, boolean lemmatize) {
    return AidaManager.getTasksInstance().tokenize(docId, text, Tokenizer.type.ner, lemmatize);
  }

  /**
   * tokenizes the text with POS
   * 
   * @param docId
   * @param text
   * @return
   */
  public static Tokens tokenizePOS(String docId, String text, boolean lemmatize) {
    return AidaManager.getTasksInstance().tokenize(docId, text, Tokenizer.type.pos, lemmatize);
  }

  /**
   * tokenizes the text with PARSE
   * 
   * @param docId
   * @param text
   * @return
   */
  public static Tokens tokenizePARSE(String docId, String text, boolean lemmatize) {
    return AidaManager.getTasksInstance().tokenize(docId, text, Tokenizer.type.parse, lemmatize);
  }

  /**
   * tokenizes the text with PARSE
   * 
   * @param docId
   * @param text
   * @return
   */
  public static Tokens tokenizePARSE(String docId, String text, Tokenizer.type type, boolean lemmatize) {
    return AidaManager.getTasksInstance().tokenize(docId, text, type, lemmatize);
  }

  public static synchronized DBConnection getConnectionForDatabase(String dbId, String req) throws SQLException {
    if (!MultipleDBManager.isConnected(dbId)) {
      DBSettings settings = new DBSettings(dbIdToConfig.get(dbId));
      MultipleDBManager.addDatabase(dbId, settings);
    }
    return MultipleDBManager.getConnection(dbId, req);
  }

  public static void releaseConnection(String dbId, DBConnection con) {
    MultipleDBManager.releaseConnection(dbId, con);
  }
  
  /**
   * Gets an AIDA entity for the given YAGO entity id.
   * 
   * @param yagoEntityId  ID in YAGO2 format
   * @return              AIDA Entity
   */
  public static Entity getEntity(String yagoEntityId) {
    return new Entity(yagoEntityId, DataAccess.getIdForYagoEntityId(yagoEntityId));
  }
  
  /**
   * Gets an AIDA entity for the given AIDA entity id.
   * 
   * @param entityId  Internal AIDA int ID 
   * @return          AIDA Entity
   */
  public static Entity getEntity(int entityId) {
    return new Entity(DataAccess.getYagoEntityIdForId(entityId), entityId);
  }
  
  /**
   * Creates the Wikipedia link for a given ResultEntity.
   * 
   * @param resultEntity  Given AIDA ResultEntity.
   * @return  Wikipedia Link for the entity.
   */
  public static String getWikipediaUrl(ResultEntity resultEntity) {
    Entity entity = getEntity(resultEntity.getEntity());
    String titlePart = Normalize.unEntity(entity.getName()).replace(' ', '_');
    return WIKIPEDIA_PREFIX + titlePart;
  }
  
  /**
   * Creates the YAGO identifier for a given ResultEntity.
   * 
   * @param resultEntity  Given AIDA ResultEntity.
   * @return  YAGO Resource for the entity.
   */
  public static String getYAGOIdentifier(ResultEntity resultEntity) {
    Entity entity = getEntity(resultEntity.getEntity());
    String titlePart = Normalize.unEntity(entity.getName()).replace(' ', '_');
    return YAGO_PREFIX + titlePart;
  }
    
  /**
   * Creates the Wikipedia link for a given entity.
   * 
   * @param entity  Given AIDA entity.
   * @return  Wikipedia Link for the entity.
   */
  public static String getWikipediaUrl(Entity entity) {
    String titlePart = Normalize.unEntity(entity.getName()).replace(' ', '_');
    return WIKIPEDIA_PREFIX + titlePart;
  }
  
  /**
   * Creates the YAGO identifier for a given entity.
   * 
   * @param entity  Given AIDA entity.
   * @return  YAGO Resource for the entity.
   */
  public static String getYAGOIdentifier(Entity entity) {
    String titlePart = Normalize.unEntity(entity.getName()).replace(' ', '_');
    return YAGO_PREFIX + titlePart;
  }  
  
  
  /**
   * Returns the potential entity candidates for a mention (via the YAGO
   * 'means' relation)
   * 
   * @param mention
   *            Mention to get entity candidates for
   * @return Candidate entities for this mention (in YAGO2 encoding) including
   *         their prior probability
   * 
   */
  public static Entities getEntitiesForMention(String mention) {
    return DataAccess.getEntitiesForMention(mention);
  }
  /**
   * Returns the potential entity candidates for a mention (via the YAGO
   * 'means' relation)
   * 
   * @param mention
   *            Mention to get entity candidates for
   * @return Candidate entities for this mention (in YAGO2 encoding) including
   *         their prior probability
   * 
   */
  public static Entities getEntitiesForMention(Mention mention) {
    return DataAccess.getEntitiesForMention(mention.getMention());
  }

  /**
   * Returns the potential entity candidates for a mention (via the YAGO
   * 'means' relation) and filters those candidates against the given list of
   * types
   * 
   * @param mention
   *            Mention to get entity candidates for
   * @return Candidate entities for this mention (in YAGO2 encoding) including
   *         their prior probability
   * @throws SQLException
   */
  public static Entities getEntitiesForMention(Mention  mention, List<String> filteringTypes) throws SQLException {
    Entities entities = getEntitiesForMention(mention);
    Entities filteredEntities = new Entities();
    for (Entity entity : entities) {
      String entityName = entity.getName();
      List<String> entityTypes = DataAccess.getTypes(entityName);
      for (String filteringType : filteringTypes) {
        if (entityTypes.contains(filteringType)) {
          filteredEntities.add(entity);
          break;
        }
      }
    }
    return filteredEntities;
  }

  
  public static Map<String, Gender> getGenderForEntities(Entities entities) {
    return DataAccess.getGenderForEntities(entities);
  }

  
  public static void fillInCandidateEntities(Mentions mentions) throws SQLException {
    fillInCandidateEntities(null, mentions, false, false);
  }

  public static void fillInCandidateEntities(String docId, Mentions mentions, boolean includeNullEntityCandidates, boolean includeContextMentions) throws SQLException {
    List<String> filteringTypes = mentions.getEntitiesTypes();
    for (int i = 0; i < mentions.getMentions().size(); i++) {
      Mention m = mentions.getMentions().get(i);
      Entities mentionCandidateEntities;
      if (malePronouns.contains(m.getMention()) || femalePronouns.contains(m.getMention())) setCandiatesFromPreviousMentions(mentions, i);
      else {

        if (filteringTypes != null) {
        	mentionCandidateEntities = AidaManager.getEntitiesForMention(m, filteringTypes);
        } else {
        	mentionCandidateEntities = AidaManager.getEntitiesForMention(m);
	    } 
        

        if (includeNullEntityCandidates) {
          Entity nmeEntity = new Entity(Entities.getMentionNMEKey(m.getMention()), -1);

          // add surrounding mentions as context
          if (includeContextMentions) {
            List<String> surroundingMentionsNames = new LinkedList<String>();
            int begin = Math.max(i - 2, 0);
            int end = Math.min(i + 3, mentions.getMentions().size());

            for (int s = begin; s < end; s++) {
              if (s == i) continue; // skip mention itself
              surroundingMentionsNames.add(mentions.getMentions().get(s).getMention());
            }
            nmeEntity.setSurroundingMentionNames(surroundingMentionsNames);
          }

          mentionCandidateEntities.add(nmeEntity);
        }
        m.setCandidateEntities(mentionCandidateEntities);
      }
    }
  }



  private static void setCandiatesFromPreviousMentions(Mentions mentions, int mentionIndex) {
    Mention mention = mentions.getMentions().get(mentionIndex);
    Entities allPrevCandidates = new Entities();
    if (mentionIndex == 0) {
      mention.setCandidateEntities(allPrevCandidates);
      return;
    }

    for (int i = 0; i < mentionIndex; i++) {
      Mention m = mentions.getMentions().get(i);
      for (Entity e : m.getCandidateEntities()) {
        allPrevCandidates.add(new Entity(e.getName(), e.getId()));
      }
    }

    Map<String, Gender> entitiesGenders = AidaManager.getGenderForEntities(allPrevCandidates);

    Gender targetGender = null;
    if (malePronouns.contains(mention.getMention())) targetGender = Gender.MALE;
    else if (femalePronouns.contains(mention.getMention())) targetGender = Gender.FEMALE;

    Entities filteredCandidates = new Entities();
    for (Entity e : allPrevCandidates) {
      if (entitiesGenders != null && entitiesGenders.containsKey(e.getName()) && entitiesGenders.get(e.getName()) == targetGender) filteredCandidates.add(e);
    }
    mention.setCandidateEntities(filteredCandidates);
  }

  public static boolean isNamedEntity(String entity) {
    return AidaManager.getTasksInstance().checkIsNamedEntity(entity);
  }

  public static void main(String[] args) throws SQLException {
    Entities entities = getEntitiesForMention("Germany");
    for (Entity entity : entities) {
      System.out.println(entity.getName());
    }
  }

  // singleton class methods private methods  
  
  private FilterMentions filterMention = null;

  private AidaManager() {
    filterMention = new FilterMentions();
    AidaTokenizerManager.init();
    initWordExpansion();
  }

  private PreparedInput createPrepareInputData(String text, String docId, FilterType by) {
    PreparedInput preparedInput = null;
    if (by.equals(FilterType.Manual) || by.equals(FilterType.Hybrid)) {
      preparedInput = filterMention.filter(text, docId, null, by);
    } else {
      Tokens tokens = tokenize(docId, text, Tokenizer.type.ner, false);
      preparedInput = filterMention.filter(text, docId, tokens, by);
      preparedInput.setTokens(tokens);
    }
    return preparedInput;
  }

  private Tokens tokenize(String docId, String text, Tokenizer.type type, boolean lemmatize) {
    return AidaTokenizerManager.tokenize(docId, text, type, lemmatize);
  }

  private boolean checkIsNamedEntity(String entity) {
    if (Normalize.unWordNetEntity(entity) == null 
        && Normalize.unWikiCategory(entity) == null 
        && Normalize.unGeonamesClass(entity) == null 
        && Normalize.unGeonamesEntity(entity) == null) {
      return true;
    } else {
      return false;
    }
  }

  public static String expandTerm(String term) {
    return term.toUpperCase(Locale.ENGLISH);
  }

  /** 
   Gets the ALL_UPPERCASE wordId for the input wordId.
   NEED TO CALL INIT FIRST!.
   */
  public static int expandTerm(int wordId) {
    return wordExpansions[wordId];
  }
}
