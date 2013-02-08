package mpi.aida.graph.similarity.context;

import gnu.trove.map.hash.TIntObjectHashMap;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;

/**
 * Abstract class for all contexts containing solely integer ids 
 * representing tokens. 
 * 
 *
 */
public abstract class TextContext extends EntitiesContext {

  private TIntObjectHashMap<int[]> entityTokens;

  public TextContext(Entities entities, EntitiesContextSettings settings) throws Exception {
    super(entities, settings);
  }

  @Override
  public int[] getContext(Entity entity) {
    return entityTokens.get(entity.getId());
  }
  
  @Override
  protected void setupEntities(Entities entities) throws Exception {
    entityTokens = new TIntObjectHashMap<int[]>();

    for (int entity : entities.getUniqueIds()) {
      entityTokens.put(entity, getTextTokens(entity));
    }
  }

  protected abstract int[] getTextTokens(int entity);
}
