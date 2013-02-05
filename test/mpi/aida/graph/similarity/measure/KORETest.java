package mpi.aida.graph.similarity.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import mpi.aida.access.DataAccess;
import mpi.aida.config.AidaConfig;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.graph.similarity.EntityEntitySimilarity;
import mpi.aida.graph.similarity.context.EntitiesContextSettings;
import mpi.experiment.trace.NullTracer;

import org.junit.Test;


public class KORETest {
  public KORETest() {
    AidaConfig.set("dataAccess", "testing");
  }
  
  @Test
  public void koreTest() throws Exception {
    Entity a = new Entity("Kashmir_(song)", DataAccess.getIdForYagoEntityId("Kashmir_(song)"));
    Entity b = new Entity("Jimmy_Page", DataAccess.getIdForYagoEntityId("Jimmy_Page"));
    Entity c = new Entity("Larry_Page", DataAccess.getIdForYagoEntityId("Larry_Page"));
    Entity d = new Entity("Knebworth_Festival", DataAccess.getIdForYagoEntityId("Knebworth_Festival"));
    
    Entities entities = new Entities();
    entities.add(a);
    entities.add(b);
    entities.add(c);
    entities.add(d);

    EntitiesContextSettings ecs = new EntitiesContextSettings();
    ecs.setEntityCoherenceKeyphraseAlpha(1.0);
    ecs.setEntityCoherenceKeywordAlpha(0.0);
    ecs.setShouldNormalizeWeights(true);
    ecs.setKeyphraseSourceExclusion(DataAccess.KPSOURCE_INLINKTITLE);
    EntityEntitySimilarity kore = 
        EntityEntitySimilarity.getKOREEntityEntitySimilarity(
            entities, ecs, new NullTracer());

    double simAB = kore.calcSimilarity(a, b);
    double simAC = kore.calcSimilarity(a, c);
    double simBD = kore.calcSimilarity(b, d);
    double simCD = kore.calcSimilarity(c, d);
    double simAD = kore.calcSimilarity(a, d);
    
    assertTrue(simAB > simAC);
    assertTrue(simAD < simAB);
    assertTrue(simBD > simCD);
    assertEquals(0.2091, simAB, 0.0001);
    assertEquals(0.1125, simBD, 0.0001);
    assertEquals(0.1613, simAD, 0.0001);
    assertEquals(0.0, simCD, 0.001);
  }
}
