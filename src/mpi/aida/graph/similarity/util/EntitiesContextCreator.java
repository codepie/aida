 package mpi.aida.graph.similarity.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import mpi.aida.data.Entities;
import mpi.aida.graph.similarity.context.EntitiesContext;

/** 
 * Caches entity contexts based on the context id and document id.
 * Assumes distinct document ids and caches up to ecc contexts.
 * 
 *
 */
public class EntitiesContextCreator {
  /** Has to be at least 1. */
  private static final int CACHE_SIZE = 10;
  
  /** Holds the cached EntityContexts. */
  private Map<String, EntitiesContext> cache = 
      new HashMap<String, EntitiesContext>();
  
  /** 
   * Keeps the order in which the EntityContexts were created for 
   * discarding the least recently used on cache overflow.
   */
  private List<String> cacheIds = new LinkedList<String>();

  /**
   * Synchronized the creation of different contexts. Allows the parallel
   * creation of contexts for distinct documents but blocks for requests
   * of the same context. 
   */
  private Map<String, Lock> contextCreationLocks = new HashMap<String, Lock>();
  
  private static class EntitiesContextCreatorHolder {
    public static EntitiesContextCreator ecc = new EntitiesContextCreator();
  }

  public static EntitiesContextCreator getEntitiesContextCache() {
    return EntitiesContextCreatorHolder.ecc;
  }
    
  public EntitiesContext getEntitiesContext(
      String contextClassName, String docId, Entities entities) 
          throws Exception {
    
    String id = getCacheId(contextClassName, docId);
    
    // Allow the parallel creation of distinct contexts but only
    // one creation per id.
    Lock contextLock = getContextCreationLock(id);
    contextLock.lock();
    EntitiesContext context = null;
    try {
      context = cache.get(id);
      
      if (context == null) {
        // Create context.
        context = 
            (EntitiesContext) 
            Class.forName(contextClassName).
              getDeclaredConstructor(Entities.class).newInstance(entities);
        
        // Put it into the cache, deleting the oldest cache if the cache
        // size is exceeded.
        synchronized(cache) {
          cache.put(id, context);
          cacheIds.add(id);
          
          if (cacheIds.size() > CACHE_SIZE) {
            String removedId = cacheIds.get(0);
            cacheIds.remove(0);
            cache.remove(removedId);
          }
        }
      }
    } catch (Exception e) {
      throw e;    
    } finally {
      contextLock.unlock();
    }
    
    // Will be null if something goes wrong in the creation process.
    return context;
  }
  
  private String getCacheId(String contextClassName, String docId) {
    return contextClassName + "\t" + docId;
  }

  private synchronized Lock getContextCreationLock(String id) {
    Lock lock = contextCreationLocks.get(id);
    if (lock == null) {
      lock = new ReentrantLock();
      contextCreationLocks.put(id, lock);
    }
    return lock;
  }
}
