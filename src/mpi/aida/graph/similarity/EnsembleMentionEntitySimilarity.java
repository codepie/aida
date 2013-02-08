package mpi.aida.graph.similarity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mpi.aida.data.Context;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.data.Mention;
import mpi.aida.data.Mentions;
import mpi.aida.graph.similarity.importance.EntityImportance;
import mpi.aida.graph.similarity.util.SimilaritySettings;
import mpi.experiment.trace.GraphTracer;
import mpi.experiment.trace.Tracer;
import mpi.experiment.trace.measures.PriorMeasureTracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uses learned weights for all MentionEntitySimilarityMeasure X EntityContext
 * combination to create a combined MentionEntitySimilarity measure.
 * 
 * The prior probability of a mention-entity pair can also be used, and should
 * generally improve results.
 * 
 *
 */
public class EnsembleMentionEntitySimilarity {
  private static final Logger logger = 
      LoggerFactory.getLogger(EnsembleMentionEntitySimilarity.class);
  
  private List<MentionEntitySimilarity> mes;

  private List<EntityImportance> eis;

  private PriorProbability pp = null;

  private SimilaritySettings settings;

  private String docId = null;

  private Tracer tracer = null;

  public EnsembleMentionEntitySimilarity(Mentions mentions, Entities entities, SimilaritySettings settings, Tracer tracer) throws Exception {
    this(mentions, entities, settings, null, tracer);
  }

  public EnsembleMentionEntitySimilarity(Mentions mentions, Entities entities, SimilaritySettings settings, String docId, Tracer tracer) throws Exception {
    this.settings = settings;
    double prior = settings.getPriorWeight();
    Set<String> mentionNames = new HashSet<String>();
    for (Mention m : mentions.getMentions()) {
      mentionNames.add(m.getMention());
    }
    pp = new MaterializedPriorProbability(mentionNames);
    pp.setWeight(prior);
    mes = settings.getMentionEntitySimilarities(entities, docId, tracer);
    // adjust weights when switched
    if (settings.getPriorThreshold() >= 0.0) {
      double[] nonPriorWeights = new double[mes.size() / 2];
      for (int i = 0; i < mes.size() / 2; i++) {
        nonPriorWeights[i] = mes.get(i).getWeight();
      }
      double[] normNonPriorWeights = rescaleArray(nonPriorWeights);
      for (int i = 0; i < mes.size() / 2; i++) {
        mes.get(i).setWeight(normNonPriorWeights[i]);
      }

      double[] withPriorWeights = new double[mes.size() / 2 + 1];

      for (int i = mes.size() / 2; i < mes.size(); i++) {
        withPriorWeights[i - mes.size() / 2] = mes.get(i).getWeight();
      }

      withPriorWeights[withPriorWeights.length - 1] = pp.getWeight();

      double[] normWithPriorWeights = rescaleArray(withPriorWeights);
      for (int i = mes.size() / 2; i < mes.size(); i++) {
        mes.get(i).setWeight(normWithPriorWeights[i - mes.size() / 2]);
      }
      pp.setWeight(normWithPriorWeights[normWithPriorWeights.length - 1]);
    }
    eis = settings.getEntityImportances(entities);
    this.docId = docId;
    this.tracer = tracer;
  }

  public static double[] rescaleArray(double[] in) {
    double[] out = new double[in.length];

    double total = 0;

    for (double i : in) {
      total += i;
    }

    // rescale

    for (int i = 0; i < in.length; i++) {
      double norm = in[i] / total;
      out[i] = norm;
    }

    return out;
  }

  public double calcSimilarity(Mention mention, Context context, Entity entity) throws Exception {
    if (settings.getPriorThreshold() >= 0.0) {
      return calcSwitchedSimilarity(mention, context, entity);
    }

    double weightedSimilarity = 0.0;

    for (MentionEntitySimilarity s : mes) {
      double singleSimilarity = s.calcSimilarity(mention, context, entity, docId) * s.getWeight();
      singleSimilarity = rescale(singleSimilarity, settings.getMin(s.getIdentifier()), settings.getMax(s.getIdentifier()));

      weightedSimilarity += singleSimilarity;
    }

    for (EntityImportance ei : eis) {
      double singleImportance = ei.getImportance(entity) * ei.getWeight();
      singleImportance = rescale(singleImportance, settings.getMin(ei.toString()), settings.getMax(ei.toString()));
      weightedSimilarity += singleImportance;
    }

    if (pp != null && settings.getPriorWeight() > 0.0) {
      double weightedPrior = pp.getPriorProbability(mention.getMention(), entity);
      weightedPrior = rescale(weightedPrior, settings.getMin("prior"), settings.getMax("prior"));

      weightedSimilarity += weightedPrior * pp.getWeight();

      PriorMeasureTracer mt = new PriorMeasureTracer("Prior", pp.getWeight());
      mt.setScore(weightedPrior);
      tracer.addMeasureForMentionEntity(mention, entity.getName(), mt);
    }
    
    tracer.setMentionEntityTotalSimilarityScore(mention, entity.getName(), weightedSimilarity);

    return weightedSimilarity;
  }


/**
   * First 4 similarity measures MUST BE the switched ones. All other ones are just used normally
   * 
   * @param mention
   * @param context
   * @param begin
   * @param end
   * @param entity
   * @return
   * @throws Exception
   */
  private double calcSwitchedSimilarity(Mention mention, Context context, Entity entity) throws Exception {
    double bestPrior = pp.getBestPrior(mention.getMention());

    double weightedSimilarity = 0.0;

    if (!shouldIncludePrior(bestPrior, mention)) {
      for (int i = 0; i < mes.size() / 2; i++) {
        MentionEntitySimilarity s = mes.get(i);
        double singleSimilarity = s.calcSimilarity(mention, context, entity, docId);
        singleSimilarity = rescale(singleSimilarity, settings.getMin(s.getIdentifier()), settings.getMax(s.getIdentifier()));

        weightedSimilarity += singleSimilarity * s.getWeight();
      }
    } else {
      GraphTracer.gTracer.addMentionToLocalIncludingPrior(docId, mention.getMention(), mention.getStartToken());

      for (int i = mes.size() / 2; i < mes.size(); i++) {
        MentionEntitySimilarity s = mes.get(i);
        double singleSimilarity = s.calcSimilarity(mention, context, entity, docId);
        singleSimilarity = rescale(singleSimilarity, settings.getMin(s.getIdentifier()), settings.getMax(s.getIdentifier()));

        weightedSimilarity += singleSimilarity * s.getWeight();
      }

      double prior = pp.getPriorProbability(mention.getMention(), entity);
      double weightedPrior = rescale(prior, settings.getMin("prior"), settings.getMax("prior"));

      weightedSimilarity += weightedPrior * pp.getWeight();

      PriorMeasureTracer mt = new PriorMeasureTracer("Prior", pp.getWeight());
      mt.setScore(weightedPrior);
      tracer.addMeasureForMentionEntity(mention, entity.getName(), mt);
    }
    
//    if(settings.isUseHYENA()) {
//    	double typeClassificationRescalingWeight = calcTypeClassficiationRescalingWeight(mention,tokens, entity);
//    	weightedSimilarity *= typeClassificationRescalingWeight;
//    }

    tracer.setMentionEntityTotalSimilarityScore(mention, entity.getName(), weightedSimilarity);

    return weightedSimilarity;
  }

  private boolean shouldIncludePrior(double bestPrior, Mention mention) {
    boolean shouldUse = bestPrior > settings.getPriorThreshold();

    if (!shouldUse) {
      return false;
    } else {
      // make sure that at least 10% of all candidates have a prior to make up for lacking data
      int total = 0;
      int withPrior = 0;

      for (Entity e : mention.getCandidateEntities()) {
        total++;

        if (pp.getPriorProbability(mention.getMention(), e) > 0.0) {
          withPrior++;
        }
      }

      double priorRatio = (double) withPrior / (double) total;

      if (priorRatio >= 0.2) {
        return true;
      } else {
        return false;
      }
    }
  }

  private double rescale(double value, double min, double max) {
    if (value < min) {
      logger.debug("Wrong normalization, " + 
                    value + " not in [" + min + "," + max + "], " +
                   "renormalizing to 0.0.");
      return 0.0;
    } else if (value > max) {
      logger.debug("Wrong normalization, " + 
          value + " not in [" + min + "," + max + "], " +
         "renormalizing to 1.0.");
      return 1.0;
    }
    return (value - min) / (max - min);
  }
  
  public void announceMentionAssignment(Mention mention, Entity entity) {
	  for(MentionEntitySimilarity mesInstance: mes) {
		  mesInstance.announceMentionAssignment(mention, entity);
	  }
  }
  public void addExtraContext(Mention mention, Object context) {
	  for(MentionEntitySimilarity mesInstance: mes) {
		  mesInstance.addExtraContext(mention, context);
	  }
  }
}
