package mpi.aida.graph.similarity.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import mpi.aida.access.DataAccessSQL;
import mpi.aida.data.Entities;
import mpi.aida.graph.similarity.EntityEntitySimilarity;
import mpi.aida.graph.similarity.MentionEntitySimilarity;
import mpi.aida.graph.similarity.context.EntitiesContext;
import mpi.aida.graph.similarity.context.EntitiesContextSettings;
import mpi.aida.graph.similarity.exception.MissingSettingException;
import mpi.aida.graph.similarity.importance.EntityImportance;
import mpi.aida.graph.similarity.measure.MentionEntitySimilarityMeasure;
import mpi.experiment.trace.Tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Settings for computing the weights of the disambiguation graph.
 * Preconfigured settings are in the 
 * {@see mpi.aida.config.settings.disambiguation}
 * package.
 * 
 * Can be created programmatically or read from a file.
 * 
 * File format:
 * 
 * - mentionEntitySimilarities
 *   - a list of space-separated mention-entity similarity triples, 
 *   separated by ":". The first one is the SimilarityMeasure, the second the 
 *   EntitiesContext, the third the weight of this mentionEntitySimilarity. 
 *   Note that they need to add up to 1.0, including the number for the 
 *   priorWeight option. The mentionEntitySimilarities option also allows to 
 *   enable or disable the first or second half of the mention-entity 
 *   similarities based on the priorThreshold option. If this is present, 
 *   the first half of the list is used when the prior is disable, 
 *   the second one when it is enabled. Note that still the whole list weights 
 *   need to sum up to 1 with the prior, the EnsembleMentionEntitySimilarity 
 *   class will take care of appropriate re-scaling.
 * - priorWeight
 *   - The weight of the prior probability. Needs to sum up to 1.0 with all 
 *   weights in mentionEntitySimilarities.
 * - priorThreshold
 *   -If set, the first half of mentionEntitySimilarities will be used for 
 *   the mention-entity similarity when the best prior for an entity candidate 
 *   is below the given threshold, otherwise the second half of the list 
 *   together with the prior is used.
 * - entityEntitySimilarity
 *   - The name and the weight of the entity-entity similarity to use, 
 *   ":" separated.
 * 
 *
 */
public class SimilaritySettings implements Serializable {
  private static final Logger logger = 
      LoggerFactory.getLogger(SimilaritySettings.class);

  private static final long serialVersionUID = 5712963955706453268L;

  /** 
   * Mmention-entity-similarity triples. The first entry is the
   * SimilarityMeasure, the second one the EntitiesContext, the third one the
   * weight.
   */
  private List<String[]> mentionEntitySimilarities = new LinkedList<String[]>();

  /** Entity importance-weight pairs. */
  private List<String[]> entityImportancesSettings = new LinkedList<String[]>();

  /** 
   * Entity-Entity-Similarity tuples. First entry is the EESim id, second the
   * weight.
   */
  private List<String[]> entityEntitySimilarities = new LinkedList<String[]>();

  /** Weight of the prior probability. */ 
  private double priorWeight;

  /** Threshold above which the prior should be considered. */
  private double priorThreshold = -1;
  
  private int numberOfEntityKeyphrase;
  
  private double entityCohKeyphraseAlpha;   
  private double entityCohKeywordAlpha;
  private boolean normalizeCoherenceWeights;
  private boolean shouldAverageCoherenceWeights;
  private boolean useConfusableMIWeights;
  
  // LSH configuration.
  private int lshBandSize;
  private int lshBandCount;
  private String lshDatabaseTable;
  
  private int nGramLength;
  
  private String keyphraseSourceExclusion;

  
  public String getKeyphraseSourceExclusion() {
    return keyphraseSourceExclusion;
  }

  
  public void setKeyphraseSourceExclusion(String keyphraseSourceExclusion) {
    this.keyphraseSourceExclusion = keyphraseSourceExclusion;
  }

  /**
   * Maximum and minimum for each mentionEntitySimilarity in use. Needed
   * for normalization.   
   */
  private MaxMinSettings mms;

  private String identifier;

  private String fullPath;
  

  /**
   * mentionEntitySimilarities in property file need to be in the following format:
   * 
   * similarityMeasure:entityContext:weight
   * 
   * @param propertiesFilePath
   */
  public SimilaritySettings(File propertiesFile) {
    String name = propertiesFile.getName();
    identifier = name.substring(0, name.lastIndexOf('.'));

    Properties prop = new Properties();
    try {
      if (propertiesFile.exists()) {
        prop.load(new FileReader(propertiesFile));

        priorWeight = Double.parseDouble(prop.getProperty("priorWeight", "0.0"));

        priorThreshold = Double.parseDouble(prop.getProperty("priorThreshold", "-1.0"));
        
        numberOfEntityKeyphrase = Integer.parseInt(prop.getProperty("numberOfEntityKeyphrase", String.valueOf(Integer.MAX_VALUE)));
        entityCohKeyphraseAlpha = Double.parseDouble(prop.getProperty("entityCoherenceKeyphraseAlpha", String.valueOf(EntitiesContextSettings.DEFAULT_KEYPHRASE_ALPHA)));
        entityCohKeywordAlpha = Double.parseDouble(prop.getProperty("entityCoherenceKeywordAlpha", String.valueOf(EntitiesContextSettings.DEFAULT_KEYWORD_ALPHA)));
        normalizeCoherenceWeights = Boolean.parseBoolean(prop.getProperty("normalizeCoherenceWeights", "false"));
        shouldAverageCoherenceWeights = Boolean.parseBoolean(prop.getProperty("shouldAverageCoherenceWeights", "false"));
        useConfusableMIWeights = Boolean.parseBoolean(prop.getProperty("useConfusableMIWeights", "false"));
        nGramLength = Integer.parseInt(prop.getProperty("nGramLength", String.valueOf(2)));
        
        // LSH config
        lshBandSize = Integer.parseInt(prop.getProperty("lshBandSize", "2"));
        lshBandCount = Integer.parseInt(prop.getProperty("lshBandCount", "10"));
        lshDatabaseTable = prop.getProperty("lshDatabaseTable", DataAccessSQL.ENTITY_LSH_SIGNATURES);

        if (prop.containsKey("keyphraseSourceExclusion")) {
          keyphraseSourceExclusion = prop.getProperty("keyphraseSourceExclusion");
        }
        
        String mentionEntitySimilarityString = prop.getProperty("mentionEntitySimilarities");

        if (mentionEntitySimilarityString != null) {
          for (String sim : mentionEntitySimilarityString.split(" ")) {
            mentionEntitySimilarities.add(sim.split(":"));
          }
        } else {
          System.err.println("No mention-entity similarity setting given in the settings file - this almost always needed!");
        }

        String entityImportanceString = prop.getProperty("entityImportanceWeights");

        if (entityImportanceString != null) {
          for (String imp : entityImportanceString.split(" ")) {
            entityImportancesSettings.add(imp.split(":"));
          }
        }

        String entityEntitySimilarityString = prop.getProperty("entityEntitySimilarity");

        if (entityEntitySimilarityString != null) {
          for (String sim : entityEntitySimilarityString.split(" ")) {
            entityEntitySimilarities.add(sim.split(":"));
          }
        }

        fullPath = propertiesFile.getAbsolutePath();

        // get max min
        String path = fullPath.substring(0, fullPath.lastIndexOf(File.separator));
        File maxMinFile = new File(path + "/maxmin.properties");

        if (!maxMinFile.exists()) {
          logger.error("No maxmin.properties file found, run StoreMaxMinFromArff to create a maxmin.properties file\n" + "in the same directory as the similaritysettings.properties");
          return;
        } else {
          mms = new MaxMinSettings(maxMinFile.getAbsolutePath());
        }

      } else {
        logger.error("Setings file specified but could not be loaded from '" + fullPath + "'");
        throw new FileNotFoundException("Setings file specified but could not be loaded from '" + fullPath + "'");
      }
    } catch (Exception e) {
      logger.error(e.getLocalizedMessage());
    }
  }

  /**
   * Constructor for programmatic access. Format of params see above
   * 
   * @param mentionEntitySimilarities
   * @param priorWeight
   * @throws MissingSettingException 
   */
  public SimilaritySettings(List<String[]> similarities, List<String[]> eeSimilarities, double priorWeight, Map<String, double[]> minMaxs) throws MissingSettingException {
    this.mentionEntitySimilarities = similarities;
    this.entityEntitySimilarities = eeSimilarities;
    this.priorWeight = priorWeight;
    this.mms = new MaxMinSettings(minMaxs);
  }

  public String getFullPath() {
    return fullPath;
  }

  public void setPriorThreshold(double priorThreshold) {
    this.priorThreshold = priorThreshold;
  }

  public double getPriorThreshold() {
    return priorThreshold;
  }

  public static String maxminFilePath = null;

  public List<MentionEntitySimilarity> getMentionEntitySimilarities(
      Entities entities, String docId, Tracer tracer) throws Exception {
    List<MentionEntitySimilarity> sims = new LinkedList<MentionEntitySimilarity>();
    Map<String, MentionEntitySimilarityMeasure> measures = new HashMap<String, MentionEntitySimilarityMeasure>();

    if (mentionEntitySimilarities != null) {
      for (String[] s : mentionEntitySimilarities) {
        boolean useDistanceDiscount = false;
  
        String[] simConfig = s[0].split(",");
        String simClassName = "mpi.aida.graph.similarity.measure." + simConfig[0];
  
        // get flags
        for (int i = 1; i < simConfig.length; i++) {
          if (simConfig[i].equals("i")) {
            useDistanceDiscount = true;
          }
        }
  
        String entityClassName = "mpi.aida.graph.similarity.context." + s[1];
        double weight = Double.parseDouble(s[2]);
  
        MentionEntitySimilarityMeasure sim = measures.get(simClassName);
        if (sim == null) {
          sim = (MentionEntitySimilarityMeasure) Class.forName(simClassName).getDeclaredConstructor(Tracer.class).newInstance(tracer);
          sim.setUseDistanceDiscount(useDistanceDiscount);
          measures.put(simClassName, sim);
        }
        
        EntitiesContext con = EntitiesContextCreator.getEntitiesContextCache().
            getEntitiesContext(entityClassName, docId, entities);
        
        MentionEntitySimilarity mes = new MentionEntitySimilarity(sim, con, weight);
        sims.add(mes);
      }
    }

    return sims;
  }

  public EntityEntitySimilarity getEntityEntitySimilarity(String eeIdentifier, Entities entities, Tracer tracer) throws Exception {
    EntityEntitySimilarity eeSim = null;
    EntitiesContextSettings settings = new EntitiesContextSettings();
    settings.setEntityCoherenceKeyphraseAlpha(entityCohKeyphraseAlpha);
    settings.setEntityCoherenceKeywordAlpha(entityCohKeywordAlpha);
    settings.setKeyphraseSourceExclusion(keyphraseSourceExclusion);
    settings.setUseConfusableMIWeight(useConfusableMIWeights);
    settings.setShouldNormalizeWeights(normalizeCoherenceWeights);
    settings.setShouldAverageWeights(shouldAverageCoherenceWeights);
    settings.setNgramLength(nGramLength);
    settings.setLshBandCount(lshBandCount);
    settings.setLshBandSize(lshBandSize);
    settings.setLshDatabaseTable(lshDatabaseTable);
    
    if (eeIdentifier.equals("MilneWittenEntityEntitySimilarity")) {
      return EntityEntitySimilarity.getMilneWittenSimilarity(entities, tracer);
    } else if (eeIdentifier.equals("InlinkOverlapEntityEntitySimilarity")) {
      return EntityEntitySimilarity.getInlinkOverlapSimilarity(entities, tracer);
//    } else if (eeIdentifier.equals("KeywordBasedEntityEntitySimilarity")) {
//      return EntityEntitySimilarity.getKeywordBasedEntityEntitySimilarity(entities, tracer);
//    } else if (eeIdentifier.equals("KeyphraseReweightedKeywordBasedEntityEntitySimilarity")) {
//      return EntityEntitySimilarity.getKeyphraseReweightedKeywordBasedEntityEntitySimilarity(entities, settings, tracer);
//    } else if (eeIdentifier.equals("TopKeywordBasedEntityEntitySimilarity")) {
//      return EntityEntitySimilarity.getTopKeywordBasedEntityEntitySimilarity(entities, settings, tracer);
    } else if (eeIdentifier.equals("KeyphraseBasedNGDEntityEntitySimilarity")) {
      return EntityEntitySimilarity.getKeyphraseBasedNGDEntityEntitySimilarity(entities, settings, tracer);
    } else if (eeIdentifier.equals("WeightedKeyphraseBasedNGDEntityEntitySimilarity")) {
      return EntityEntitySimilarity.getWeightedKeyphraseBasedNGDEntityEntitySimilarity(entities, settings, tracer);
//    } else if (eeIdentifier.equals("TimeClosenessBasedEntityEntitySimilarity")) {
//      return EntityEntitySimilarity.getTimeClosenessBasedSimilarity(entities, tracer);
//    } else if (eeIdentifier.equals("PersonTimeClosenessBasedEntityEntitySimilarity")) {
//      return EntityEntitySimilarity.getPersonTimeClosenessBasedSimilarity(entities, tracer);
//    } else if (eeIdentifier.equals("TimeOverlapBasedEntityEntitySimilarity")) {
//      return EntityEntitySimilarity.getTimeOverlapBasedSimilarity(entities, tracer);
//    } else if (eeIdentifier.equals("PersonTimeOverlapBasedEntityEntitySimilarity")) {
//      return EntityEntitySimilarity.getPersonTimeOverlapBasedSimilarity(entities, tracer);
//    } else if (eeIdentifier.equals("LocationBasedEntityEntitySimilarity")) {
//      return EntityEntitySimilarity.getLocationBasedSimilarity(entities, tracer);
    } else if (eeIdentifier.equals("JaccardKeywordEntityEntitySimilarity")) {
      return EntityEntitySimilarity.getJaccardKeywordEntityEntitySimilarity(entities, tracer);
    } else if (eeIdentifier.equals("WeightedJaccardKeyphraseEntityEntitySimilarity")) {
      return EntityEntitySimilarity.getWeightedJaccardKeyphraseEntityEntitySimilarity(entities, settings, tracer);
    } else if (eeIdentifier.equals("KeyphraseBasedEntityEntitySimilarity")) {
      return EntityEntitySimilarity.getKeyphraseBasedEntityEntitySimilarity(entities, settings, tracer);
    } else if (eeIdentifier.equals("FastPartialMaxMinKeyphraseEntityEntitySimilarity")) {
      return EntityEntitySimilarity.getFastPartialMaxMinKeyphraseEntityEntitySimilarity(entities, settings, tracer);
    } else if (eeIdentifier.equals("KOREEntityEntitySimilarity")) {
      return EntityEntitySimilarity.getKOREEntityEntitySimilarity(entities, settings, tracer);
//    } else if (eeIdentifier.equals("LSHPartialKeyphraseEntityEntitySimilarity")) {
//      return EntityEntitySimilarity.getLSHPartialKeyphraseEntityEntitySimilarity(entities, settings, tracer);
    } else if (eeIdentifier.equals("TopKeyphraseBasedEntityEntitySimilarity")) {
      return EntityEntitySimilarity.getTopKeyphraseBasedEntityEntitySimilarity(entities, numberOfEntityKeyphrase, tracer);
    } else {
      logger.error("EESimilarity '" + eeIdentifier + "' undefined");
      return eeSim;
    }
  }  
   
  public List<EntityEntitySimilarity> getEntityEntitySimilarities(Entities entities, Tracer tracer) throws Exception {
    List<EntityEntitySimilarity> eeSims = new LinkedList<EntityEntitySimilarity>();

    for (String[] s : entityEntitySimilarities) {
      String eeIdentifier = s[0];
      EntityEntitySimilarity eeSim = getEntityEntitySimilarity(eeIdentifier, entities, tracer);

      double weight = Double.parseDouble(s[1]);
      eeSim.setWeight(weight);

      eeSims.add(eeSim);
    }

    return eeSims;
  }

  public double getPriorWeight() {
    return priorWeight;
  }

  public String getIdentifier() {
    return identifier;
  }

  public double getMin(String featureName) {
    if (featureName == null) {
      logger.error("Getting min for null feature in SimilaritySettings (" + identifier + ")");
    } else if (mms == null) {
      logger.error("MaxMinSettings is null in SimilaritySettings (" + identifier + ")");
    }

    return mms.getMin(featureName);
  }

  public double getMax(String featureName) {
    if (featureName == null) {
      logger.error("Getting max for null feature in SimilaritySettings (" + identifier + ")");
    } else if (mms == null) {
      logger.error("MaxMinSettings is null in SimilaritySettings (" + identifier + ")");
    }

    return mms.getMax(featureName);
  }
  
  public double getEntityCohKeyphraseAlpha() {
    return entityCohKeyphraseAlpha;
  }

  public void setEntityCohKeyphraseAlpha(double entityCohKeyphraseAlpha) {
    this.entityCohKeyphraseAlpha = entityCohKeyphraseAlpha;
  }
  
  public double getEntityCohKeywordAlpha() {
    return entityCohKeywordAlpha;
  }

  public void setEntityCohKeywordAlpha(double entityCohKeywordAlpha) {
    this.entityCohKeywordAlpha = entityCohKeywordAlpha;
  }

  public List<EntityImportance> getEntityImportances(Entities entities) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
    List<EntityImportance> eis = new LinkedList<EntityImportance>();

    for (String[] eiSetting : entityImportancesSettings) {
      String eiClassName = "mpi.aida.graph.similarity.importance." + eiSetting[0];
      EntityImportance ei = (EntityImportance) Class.forName(eiClassName).getDeclaredConstructor(Entities.class).newInstance(entities);
      ei.setWeight(Double.parseDouble(eiSetting[1]));
    }

    return eis;
  }

  public List<String[]> getMentionEntitySimilarities() {
    return mentionEntitySimilarities;
  }

  public void setMentionEntitySimilarities(List<String[]> mentionEntitySimilarities) {
    this.mentionEntitySimilarities = mentionEntitySimilarities;
  }

  public void setPriorWeight(double priorWeight) {
    this.priorWeight = priorWeight;
  }

  public double getNormalizedAverageScore() throws FileNotFoundException, IOException {
    File parentDir = new File(fullPath).getParentFile();
    File avgPropFile = new File(parentDir, "averages.properties");
       
    if (avgPropFile.exists()) {
      Properties avgProp = new Properties();
      avgProp.load(new FileReader(avgPropFile));
      
      if (avgProp.containsKey(identifier)) {
        String[] avgMax = avgProp.getProperty(identifier).split(":");
        double avg = Double.parseDouble(avgMax[0]);
        double max = Double.parseDouble(avgMax[1]);
        double normAvg = avg / max;
        return normAvg;
      } else {
        System.err.println("Couldn't load averages for " + identifier + ", run AverageSimilarityScoresCalculator");
        return -1.0;
      }
    } else {
      System.err.println("Couldn't load averages.properties from the settings dir, run AverageSimilarityScoresCalculator");
      return -1.0;
    }
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public void setShouldNormalizeCoherenceWeights(boolean flag) {
    normalizeCoherenceWeights = flag;
  }
  
  public boolean shouldNormalizeCoherenceWeights() {
    return normalizeCoherenceWeights;
  }
  
  public boolean isUseConfusableMIWeights() {
    return useConfusableMIWeights;
  }
  
  public void setUseConfusableMIWeights(boolean useConfusableMIWeights) {
    this.useConfusableMIWeights = useConfusableMIWeights;
  }
  
  public boolean shouldAverageCoherenceWeights() {
    return shouldAverageCoherenceWeights;
  }

  public void setShouldAverageCoherenceWeights(boolean averageCoherenceWeights) {
    this.shouldAverageCoherenceWeights = averageCoherenceWeights;
  }

  public int getLshBandSize() {
    return lshBandSize;
  }

  public void setLshBandSize(int lshBandSize) {
    this.lshBandSize = lshBandSize;
  }

  public int getLshBandCount() {
    return lshBandCount;
  }
  
  public void setLshBandCount(int lshBandCount) {
    this.lshBandCount = lshBandCount;
  }
  
  public String getLshDatabaseTable() {
    return lshDatabaseTable;
  }
  
  public void setLshDatabaseTable(String lshDatabaseTable) {
    this.lshDatabaseTable = lshDatabaseTable;
  }
}
