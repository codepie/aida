package mpi.aida.graph.similarity.context;

import mpi.aida.data.Entities;
import mpi.aida.data.Entity;

public class EmptyEntitiesContext extends EntitiesContext {

  public EmptyEntitiesContext(Entities entities) throws Exception {
    super(entities, null);
  }

  @Override
  public int[] getContext(Entity entity) {
    return null;
  }

  @Override
  protected void setupEntities(Entities entities) throws Exception {
    // nothing
  }

  public String toString() {
    return "EmptyEntitiesContext";
  }
}
