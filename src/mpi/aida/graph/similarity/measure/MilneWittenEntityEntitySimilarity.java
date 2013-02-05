package mpi.aida.graph.similarity.measure;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import javaewah.EWAHCompressedBitmap;
import mpi.aida.access.DataAccess;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.graph.similarity.EntityEntitySimilarity;
import mpi.aida.graph.similarity.context.EntitiesContext;
import mpi.aida.util.YagoUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MilneWittenEntityEntitySimilarity extends EntityEntitySimilarity {
  private static final Logger logger = 
      LoggerFactory.getLogger(MilneWittenEntityEntitySimilarity.class);
  
  private TIntObjectHashMap<EWAHCompressedBitmap> entity2vector;


  public MilneWittenEntityEntitySimilarity(EntityEntitySimilarityMeasure similarityMeasure, EntitiesContext entityContext) throws Exception {
    // not needed - uses entites directly
    super(similarityMeasure, entityContext);

    setupEntities(entityContext.getEntities());
  }

  private void setupEntities(Entities entities) throws Exception {
    logger.info("Initializing MilneWittenEntityEntitySimilarity for " + 
                entities.uniqueNameSize() + " entities");

    TIntObjectHashMap<int[]> entityInlinks = 
        DataAccess.getInlinkNeighbors(entities);
    
    entity2vector = new TIntObjectHashMap<EWAHCompressedBitmap>();

    for (TIntObjectIterator<int[]> itr = entityInlinks.iterator();
        itr.hasNext(); ) {
      itr.advance();
      int entity = itr.key();
      int[] inLinks = itr.value();
          
      EWAHCompressedBitmap bs = new EWAHCompressedBitmap();
      for (int l : inLinks) {
        bs.set(l);
      }
      entity2vector.put(entity, bs);
    }
    
    logger.info("Done initializing MilneWittenEntityEntitySimilarity for " + 
                entities.uniqueNameSize() + " entities");
  }

  @Override
  public double calcSimilarity(Entity a, Entity b) throws Exception {
    EWAHCompressedBitmap bsA = entity2vector.get(a.getId());
    EWAHCompressedBitmap bsB = entity2vector.get(b.getId());

    double sizeA = bsA.cardinality();
    double sizeB = bsB.cardinality();

    double max = -1.0;
    double min = -1.0;

    if (sizeA >= sizeB) {
      max = sizeA;
      min = sizeB;
    } else {
      max = sizeB;
      min = sizeA;
    }
    
    double sim = 0.0; // default is no sim
    
    int overlap = bsA.andCardinality(bsB);
    
    if (overlap > 0) {  
      // now calc the real similarity
      double distance = (Math.log(max) - Math.log((double) overlap)) / (Math.log(YagoUtil.TOTAL_YAGO_ENTITIES) - Math.log(min));
  
      sim = 1 - distance;
      
      if (distance > 1.0) {
        // really far apart ...
        sim = 0.0;
      }
    }
      
    return sim;
  }
}
