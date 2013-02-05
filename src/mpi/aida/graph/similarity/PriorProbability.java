package mpi.aida.graph.similarity;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

import mpi.aida.data.Entity;

/**
 * This class calculates the prior probability of a mention
 * being associated with a given entity. The prior probability is based
 * on the occurrence count of links (and their anchor text as mention) with
 * a given Wikipedia/YAGO entity as target.
 * 
 * The calculation is done on the fly, so it is a bit slow. For a faster implementation,
 * use {@link MaterializedPriorProbability}.
 * 
 * It uses the 'hasInternalWikipediaLinkTo' and 'hasAnchorText' relations
 * in the YAGO2 database.
 * 
 * @author Johannes Hoffart
 *
 */
public abstract class PriorProbability {
 
  protected HashMap<String, TIntDoubleHashMap> priors;
  
  private double weight;
  
  public PriorProbability(Set<String> mentions) throws SQLException {
    setupMentions(mentions);
  }
  
  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }
  
  protected abstract void setupMentions(Set<String> mentions) throws SQLException;

  /**
   * Returns the prior probability for the given mention-entity pair.
   * If smoothing is true, it will return the lowest prior among all entities if
   * there is no real prior.
   * 
   * @param mention
   * @param entity
   * @param smoothing
   * @return
   */
  public double getPriorProbability(
      String mentionText, Entity entity, boolean smoothing) {
    mentionText = conflateMention(mentionText);
    TIntDoubleHashMap mentionPriors = priors.get(mentionText);
    
    if (mentionPriors == null) {
      throw new NoSuchElementException(
          "Mention " + mentionText + " must be passed to constructor!");
    }
    
    double entityPrior = mentionPriors.get(entity.getId());
    if (smoothing && entityPrior == 0.0) {
      double smallestPrior = 1.0;
      
      for (TIntDoubleIterator it = mentionPriors.iterator(); it.hasNext();) {
        it.advance();
        double currentPrior = it.value(); 
        if (currentPrior < smallestPrior) {
          smallestPrior = currentPrior;
        }
      }      
      entityPrior = smallestPrior;
    }
    
    return entityPrior;
  }
  
  public double getBestPrior(String mentionText) {
    mentionText = conflateMention(mentionText);
    TIntDoubleHashMap mentionPriors = priors.get(mentionText);

    double bestPrior = 0.0;
    for (TIntDoubleIterator it = mentionPriors.iterator(); it.hasNext();) {
      it.advance();
      double currentPrior = it.value();
      if (currentPrior > bestPrior) {
        bestPrior = currentPrior;
      }
    }
    
    return bestPrior;
  }
  
  public double getPriorProbability(String mentionText, Entity entity) {
    return getPriorProbability(mentionText, entity, false);
  }
  
  public static String conflateMention(String mention) {
    // conflate cases for mentions of length >= 4
    if (mention.length() >= 4) {
      mention = mention.toUpperCase(Locale.ENGLISH);
    }
    
    return mention;
  }
} 
