package mpi.aida.graph.similarity.importance;

import java.sql.SQLException;

import mpi.aida.data.Entities;
import mpi.aida.data.Entity;

/**
 * This class serves as way to get the importance of an entity
 * with regard to the complete collection, not to a specific mention (such as prior probability)
 * 
 * @author Johannes Hoffart
 *
 */
public abstract class EntityImportance {

  private Entities entities;

  private double weight = 0.0;

  public EntityImportance(Entities entities) throws SQLException {
    this.entities = entities;
    setupEntities(entities);
  }

  public Entities getEntities() {
    return entities;
  }

  protected abstract void setupEntities(Entities e) throws SQLException;

  public abstract double getImportance(Entity entity);

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }
}
