package mpi.aida.graph.similarity;

import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.graph.similarity.context.EmptyEntitiesContext;
import mpi.aida.graph.similarity.context.EntitiesContext;
import mpi.aida.graph.similarity.context.EntitiesContextSettings;
import mpi.aida.graph.similarity.context.FastWeightedKeyphrasesContext;
import mpi.aida.graph.similarity.context.WeightedKeyphrasesContext;
import mpi.aida.graph.similarity.measure.EntityEntitySimilarityMeasure;
import mpi.aida.graph.similarity.measure.InlinkOverlapEntityEntitySimilarity;
import mpi.aida.graph.similarity.measure.JaccardEntityEntitySimilarityMeasure;
import mpi.aida.graph.similarity.measure.KOREEntityEntitySimilarityMeasure;
import mpi.aida.graph.similarity.measure.KeyphraseCosineSimilarityMeasure;
import mpi.aida.graph.similarity.measure.MilneWittenEntityEntitySimilarity;
import mpi.aida.graph.similarity.measure.NGDSimilarityMeasure;
import mpi.aida.graph.similarity.measure.NullEntityEntitySimilarityMeasure;
import mpi.aida.graph.similarity.measure.WeightedJaccardEntityEntitySimilarityMeasure;
import mpi.aida.graph.similarity.measure.WeightedNGDSimilarityMeasure;
import mpi.experiment.trace.Tracer;

/**
 * Abstract class that should be used to create different ways of
 * calculating the similarity between two entities.
 * 
 * The similarity is a value between 0.0 (dissimilar) and +infinity.
 * 
 *
 */
public class EntityEntitySimilarity {

  EntityEntitySimilarityMeasure similarityMeasure;

  EntitiesContext entityContext;

  double weight;

  /**
   * Measures the similarity of two entities, described by their context.
   * The entityContext takes care of constructing it (pass an appropriate object
   * or use the get...Similarity() methods to get a predefined one.
   * The similarity calculation is taken care of by the given similarity object.
   * 
   * @param similarityMeasure Similarity measure to used for calculating the similarity
   * between two entities
   * @param entityContext     Constructs the context of the entity
   * @throws Exception
   */
  public EntityEntitySimilarity(EntityEntitySimilarityMeasure similarityMeasure, EntitiesContext entityContext) throws Exception {
    this.similarityMeasure = similarityMeasure;
    this.entityContext = entityContext;
  }

  /**
   * Calculates the similarity between entity a and b
   * 
   * @param a Entity a
   * @param b Entity b
   * @return  Similarity between a and b 
   * @throws Exception 
   */
  public double calcSimilarity(Entity a, Entity b) throws Exception {
    double sim = similarityMeasure.calcSimilarity(a, b, entityContext);
    return sim;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  /**
   * Calculates the entitiy similarity using an adjusted inlink overlap measure,
   * devised by Milne/Witten
   * 
   * @param entities  Entities to caculate the similarity for
   * @return
   * @throws Exception
   */
  public static MilneWittenEntityEntitySimilarity getMilneWittenSimilarity(Entities entities, Tracer tracer) throws Exception {
    return new MilneWittenEntityEntitySimilarity(new NullEntityEntitySimilarityMeasure(tracer), new EmptyEntitiesContext(entities));
  }

  public static InlinkOverlapEntityEntitySimilarity getInlinkOverlapSimilarity(Entities entities, Tracer tracer) throws Exception {
    return new InlinkOverlapEntityEntitySimilarity(new NullEntityEntitySimilarityMeasure(tracer), new EmptyEntitiesContext(entities));
  }

  public static EntityEntitySimilarity getKeyphraseBasedNGDEntityEntitySimilarity(Entities entities, EntitiesContextSettings settings, Tracer tracer) throws Exception {
    return new EntityEntitySimilarity(new NGDSimilarityMeasure(tracer), new WeightedKeyphrasesContext(entities, settings));
  }
  
  public static EntityEntitySimilarity getWeightedKeyphraseBasedNGDEntityEntitySimilarity(Entities entities, EntitiesContextSettings settings, Tracer tracer) throws Exception {
    return new EntityEntitySimilarity(new WeightedNGDSimilarityMeasure(tracer), new WeightedKeyphrasesContext(entities, settings));
  }

  public static EntityEntitySimilarity getKeyphraseBasedEntityEntitySimilarity(Entities entities, EntitiesContextSettings settings, Tracer tracer) throws Exception {
    return new EntityEntitySimilarity(new KeyphraseCosineSimilarityMeasure(tracer), new WeightedKeyphrasesContext(entities, settings));
  }
  
  public static EntityEntitySimilarity getTopKeyphraseBasedEntityEntitySimilarity(Entities entities, int numberOfEntityKeyphrases, Tracer tracer) throws Exception {
    EntitiesContextSettings settings = new EntitiesContextSettings();
    settings.setNumberOfEntityKeyphrases(numberOfEntityKeyphrases);
    return new EntityEntitySimilarity(new KeyphraseCosineSimilarityMeasure(tracer), new WeightedKeyphrasesContext(entities, settings));
  }
  
  public static EntityEntitySimilarity getFastPartialMaxMinKeyphraseEntityEntitySimilarity(Entities entities, EntitiesContextSettings settings, Tracer tracer) throws Exception {
    return new EntityEntitySimilarity(new KOREEntityEntitySimilarityMeasure(tracer), new FastWeightedKeyphrasesContext(entities, settings));
  }
  
  public static EntityEntitySimilarity getKOREEntityEntitySimilarity(Entities entities, EntitiesContextSettings settings, Tracer tracer) throws Exception {
    return new EntityEntitySimilarity(new KOREEntityEntitySimilarityMeasure(tracer), new FastWeightedKeyphrasesContext(entities, settings));
  }
  
//  public static EntityEntitySimilarity getLSHPartialKeyphraseEntityEntitySimilarity(Entities entities, EntitiesContextSettings settings, Tracer tracer) throws Exception {
//    return new EntityEntitySimilarity(new LSHEntityEntitySimilarityMeasure(tracer), new LSHContext(entities, settings));
//  }

  public static EntityEntitySimilarity getJaccardKeywordEntityEntitySimilarity(Entities entities, Tracer tracer) throws Exception {
    return new EntityEntitySimilarity(new JaccardEntityEntitySimilarityMeasure(tracer), new FastWeightedKeyphrasesContext(entities));
  }

  public static EntityEntitySimilarity getWeightedJaccardKeyphraseEntityEntitySimilarity(Entities entities, EntitiesContextSettings settings, Tracer tracer) throws Exception {
    return new EntityEntitySimilarity(new WeightedJaccardEntityEntitySimilarityMeasure(tracer), new WeightedKeyphrasesContext(entities, settings));
  }

  public EntityEntitySimilarityMeasure getSimilarityMeasure() {
    return similarityMeasure;
  }
  
  public EntitiesContext getContext() {
    return entityContext;
  }

  public String getIdentifier() {
    return similarityMeasure.getIdentifier() + ":" + 
           entityContext.getIdentifier();
  }
  
  public String toString() {
    return getIdentifier();
  }
}