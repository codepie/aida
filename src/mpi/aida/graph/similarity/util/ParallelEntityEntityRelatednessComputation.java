package mpi.aida.graph.similarity.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import mpi.aida.config.AidaConfig;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.data.Mention;
import mpi.aida.data.Mentions;
import mpi.aida.graph.similarity.EnsembleEntityEntitySimilarity;


public class ParallelEntityEntityRelatednessComputation {
  private int numThreads = 4; // default.
  private long totalNumCalcs = 0; // this is only valid if the object is created anew for each entitiy set - used for timing experiments
  
  public ParallelEntityEntityRelatednessComputation() {
    this(Integer.parseInt(AidaConfig.get(AidaConfig.EE_NUM_THREADS)));
  }
  
  public ParallelEntityEntityRelatednessComputation(int numThreads) {
    this.numThreads = numThreads;
  }
  
  public Map<Entity, Map<Entity, Double>> computeRelatedness(EnsembleEntityEntitySimilarity entitySimilarity, Entities entities) throws InterruptedException {
    return computeRelatedness(entitySimilarity, entities, null);
  }
    
  public Map<Entity, Map<Entity, Double>> computeRelatedness(EnsembleEntityEntitySimilarity entitySimilarity, Entities entities, Mentions mentions) throws InterruptedException {
    Map<Entity, Map<Entity, Double>> entityEntitySimilarities = Collections.synchronizedMap(new HashMap<Entity, Map<Entity, Double>>());
    
    Map<Entity, List<Mention>> entityMentionsMap = null;    
    if (mentions != null) {
       entityMentionsMap = prepareEntityMentionsMap(mentions);
    }

    List<Set<Entity>> entityPartitions = new LinkedList<Set<Entity>>();
    List<Entity> allEntities = new ArrayList<Entity>(entities.getEntities());
    
    int overall = 0;
    Set<Entity> part = null;
    int partSize = entities.uniqueNameSize() / numThreads;

    for (int currentPart = 0; currentPart < numThreads; currentPart++) {
      part = new HashSet<Entity>();
      entityPartitions.add(part);

      for (int j = 0; j < partSize; j++) {
        int total = (currentPart * partSize) + j;
        part.add(allEntities.get(total));

        overall++;
      }
    }

    // add rest to last part
    for (; overall < allEntities.size(); overall++) {
      part.add(allEntities.get(overall));
    }

    // create threads and run
    CountDownLatch cdl = new CountDownLatch(numThreads);

    List<ParallelEntityEntityRelatednessComputationThread> scs = new LinkedList<ParallelEntityEntityRelatednessComputationThread>();

    for (int i = 0; i < numThreads; i++) {
      ParallelEntityEntityRelatednessComputationThread sc = new ParallelEntityEntityRelatednessComputationThread(entityPartitions.get(i), entities, entitySimilarity, entityEntitySimilarities, entityMentionsMap, cdl);
      scs.add(sc);
      sc.start();
    }
    
    // wait for calculation to finish
    cdl.await();
    
    // sum up total number of calculations
    for (ParallelEntityEntityRelatednessComputationThread sc : scs) {
      totalNumCalcs += sc.getNumCalcs();
    }
    
    return entityEntitySimilarities;
  }
    
  private Map<Entity, List<Mention>> prepareEntityMentionsMap(Mentions mentions) {
    Map<Entity, List<Mention>> entityMentionsMap = new HashMap<Entity, List<Mention>>();
    
    for (int i = 0; i < mentions.getMentions().size(); i++) {
      Mention mention = mentions.getMentions().get(i);
      Entities entities = mention.getCandidateEntities();
      for (Entity entity : entities) {
        List<Mention> entityMentions = entityMentionsMap.get(entity);
        if (entityMentions == null) {
          entityMentions = new LinkedList<Mention>();
          entityMentionsMap.put(entity, entityMentions);
        }
        entityMentions.add(mention);
      }
    }
    
    return entityMentionsMap;
  }
 
  public long getTotalNumCalcs() {
    return totalNumCalcs;
  }  
}
