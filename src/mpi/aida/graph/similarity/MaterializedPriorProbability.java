package mpi.aida.graph.similarity;

import gnu.trove.map.hash.TIntDoubleHashMap;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;

import mpi.aida.access.DataAccess;

/**
 * This class calculates the prior probability of a mention
 * being associated with a given entity. The prior probability is based
 * on the occurrence count of links (and their anchor text as mention) with
 * a given Wikipedia/YAGO entity as target.
 * 
 * It is faster than {@link PriorProbability} because it uses a table with 
 * all the priors materialized. To get the table, run the {@link MaterializedPriorProbability}
 * main method, it will create another table in the YAGO2 database which can
 * then be used by this class. 
 *    
 *
 */
public class MaterializedPriorProbability extends PriorProbability {

  public MaterializedPriorProbability(Set<String> mentions) throws SQLException {
    super(mentions);
  }

  public void setupMentions(Set<String> mentions) throws SQLException {
    priors = new HashMap<String, TIntDoubleHashMap>();
    for (String mention : mentions) {
      mention = conflateMention(mention);
      TIntDoubleHashMap entityPriors = DataAccess.getEntityPriors(mention);
      priors.put(mention, entityPriors);
    }
  }
}
