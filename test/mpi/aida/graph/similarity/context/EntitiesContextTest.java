package mpi.aida.graph.similarity.context;

import static org.junit.Assert.assertTrue;
import mpi.aida.graph.similarity.context.EntitiesContext;

import org.junit.Test;


public class EntitiesContextTest {

  @Test
  public void testGetEntityName() {
    assertTrue(EntitiesContext.getEntityName("Riazuddin_\u0028physicist\u0029").equals("Riazuddin"));
    assertTrue(EntitiesContext.getEntityName("\u0028physicist\u0029_Riazuddin").equals("(physicist) Riazuddin"));
  }
}
