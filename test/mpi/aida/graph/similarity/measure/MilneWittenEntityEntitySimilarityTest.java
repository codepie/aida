package mpi.aida.graph.similarity.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import mpi.aida.access.DataAccess;
import mpi.aida.config.AidaConfig;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.graph.similarity.EntityEntitySimilarity;
import mpi.experiment.trace.NullTracer;

import org.junit.Test;

public class MilneWittenEntityEntitySimilarityTest {

  public MilneWittenEntityEntitySimilarityTest() {
    AidaConfig.set("dataAccess", "testing");
  }
  
  @Test
  public void mwTest() throws Exception {
    Entity a = new Entity("Kashmir_(song)", DataAccess.getIdForYagoEntityId("Kashmir_(song)"));
    Entity b = new Entity("Jimmy_Page", DataAccess.getIdForYagoEntityId("Jimmy_Page"));
    Entity c = new Entity("Larry_Page", DataAccess.getIdForYagoEntityId("Larry_Page"));
    Entity d = new Entity("Knebworth_Festival", DataAccess.getIdForYagoEntityId("Knebworth_Festival"));
    
    Entities entities = new Entities();
    entities.add(a);
    entities.add(b);
    entities.add(c);
    entities.add(d);

    EntityEntitySimilarity mwSim = 
        EntityEntitySimilarity.getMilneWittenSimilarity(
            entities, new NullTracer());

    double simAB = mwSim.calcSimilarity(a, b);
    double simAC = mwSim.calcSimilarity(a, c);
    double simBD = mwSim.calcSimilarity(b, d);
    double simCD = mwSim.calcSimilarity(c, d);
    double simAD = mwSim.calcSimilarity(a, d);
    
    assertTrue(simAB > simAC);
    assertTrue(simAD < simAB);
    assertTrue(simBD > simCD);
    assertEquals(0.9493, simAB, 0.0001);
    assertEquals(0.8987, simBD, 0.0001);
    assertEquals(0.9197, simAD, 0.0001);
    assertEquals(0.0, simCD, 0.001);
  }
}
