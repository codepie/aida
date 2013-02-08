package mpi.aida.graph.similarity.measure;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.BitSet;

import mpi.aida.AidaManager;
import mpi.aida.access.DataAccess;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.graph.similarity.EntityEntitySimilarity;
import mpi.aida.graph.similarity.context.EntitiesContext;
import mpi.database.DBConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Similarity of two entities is the number of common inlinks
 * 
 *
 */
public class InlinkOverlapEntityEntitySimilarity extends EntityEntitySimilarity {
  private static final Logger logger = 
      LoggerFactory.getLogger(InlinkOverlapEntityEntitySimilarity.class);

  private TIntObjectHashMap<int[]> entity2inlink;
  private TIntObjectHashMap<BitSet> entity2vector;

  DBConnection con;

  public InlinkOverlapEntityEntitySimilarity(EntityEntitySimilarityMeasure similarityMeasure, EntitiesContext entityContext) throws Exception {
    // not needed - uses entites directly
    super(similarityMeasure, entityContext);

    setupEntities(entityContext.getEntities());
  }

  private void setupEntities(Entities entities) throws Exception {
    if (entities.uniqueNameSize() == 0) {
      logger.info("Skipping initialization of InlinkEntityEntitySimilarity for " + entities.uniqueNameSize() + " entities");
      return;
    }

    logger.info("Initializing InlinkEntityEntitySimilarity for " + entities.uniqueNameSize() + " entities");

    con = AidaManager.getConnectionForDatabase(AidaManager.DB_AIDA, "getting inlinks");

    entity2inlink = DataAccess.getInlinkNeighbors(entities);

    // get all inlinks for all entities
    // get all inlinks for all entities
    TIntHashSet allInlinks = new TIntHashSet();

    for (int[] neighbors : entity2inlink.valueCollection()) {
      allInlinks.addAll(neighbors);
    }

    TIntArrayList allInlinksList = new TIntArrayList(allInlinks.size());
    for (int entry : allInlinksList.toArray()) {
      allInlinksList.add(entry);
    }
    allInlinksList.sort();
    
    // now create the bitvectors for each entity
    logger.info("Creating bitvectors for entities");

    entity2vector = new TIntObjectHashMap<BitSet>();

    for (int entity : entities.getUniqueIds()) {
      int[] inlinks = entity2inlink.get(entity);

      BitSet bs = new BitSet(allInlinksList.size());

      int current = 0;

      for (int inlink : inlinks) {
        // move to position of inlink in allInlinks
        while (allInlinksList.get(current) != inlink) {
          current++;
        }
        bs.set(current);
      }

      entity2vector.put(entity, bs);
    }

    AidaManager.releaseConnection(AidaManager.DB_AIDA, con);

    logger.info("Done initializing InlinkEntityEntitySimilarity");
  }

  @Override
  public double calcSimilarity(Entity a, Entity b) throws Exception {
    BitSet bsA = entity2vector.get(a.getId());
    BitSet bsB = entity2vector.get(b.getId());

    BitSet intersection = (BitSet) bsA.clone();
    intersection.and(bsB);

    BitSet union = (BitSet) bsA.clone();
    union.or(bsB);

    if (intersection.cardinality() == 0 || union.cardinality() == 0) {
      return 0.0; // cannot calc
    }

    double sim = (double) intersection.cardinality() 
                 / (double) union.cardinality();
    
    return sim;
  }
  
  public String toString() {
    return "InlinkOverlapEntityEntitySimilarity";
  }
}
