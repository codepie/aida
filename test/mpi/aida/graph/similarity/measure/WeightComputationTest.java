package mpi.aida.graph.similarity.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import mpi.aida.graph.similarity.measure.WeightComputation;

import org.junit.Test;


public class WeightComputationTest {

  @Test
  public void testComputeNPMI() {
    double npmi;
    npmi = WeightComputation.computeNPMI(1, 1, 1, 10);
    assertEquals(1.0, npmi, 0.001);

    npmi = WeightComputation.computeNPMI(1, 1, 0, 10);
    assertEquals(-1.0, npmi, 0.001);
    
    assertTrue(WeightComputation.computeNPMI(3, 3, 2, 10)
        > WeightComputation.computeNPMI(3, 3, 1, 10));
  }
}
