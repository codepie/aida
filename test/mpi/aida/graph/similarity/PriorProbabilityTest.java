package mpi.aida.graph.similarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import mpi.aida.AidaManager;
import mpi.aida.config.AidaConfig;
import mpi.aida.data.Entity;
import mpi.aida.graph.similarity.MaterializedPriorProbability;
import mpi.aida.graph.similarity.PriorProbability;

import org.junit.Test;

public class PriorProbabilityTest {
  
  public PriorProbabilityTest() {
    AidaConfig.set("dataAccess", "testing");
  }
  
  @Test
  public void test() throws Exception {
    Set<String> mentions = new HashSet<String>();
    mentions.add("Kashmir");
    mentions.add("Page");
    
    Entity kashmir = AidaManager.getEntity("Kashmir");
    Entity kashmirSong = AidaManager.getEntity("Kashmir_(song)");
    Entity jimmy = AidaManager.getEntity("Jimmy_Page");
    Entity larry = AidaManager.getEntity("Larry_Page");
    
    PriorProbability pp = new MaterializedPriorProbability(mentions);
    
    double ppKashmirKashmir = pp.getPriorProbability("Kashmir", kashmir);
    double ppKashmirKashmirSong = pp.getPriorProbability("Kashmir", kashmirSong);
        
    assertTrue(ppKashmirKashmir > ppKashmirKashmirSong);
    assertEquals(0.9, ppKashmirKashmir, 0.001);
    assertEquals(1.0, ppKashmirKashmir + ppKashmirKashmirSong, 0.001);

    double ppPageJimmy = pp.getPriorProbability("Page", jimmy);
    double ppPageLarry = pp.getPriorProbability("Page", larry);
    
    assertTrue(ppPageJimmy < ppPageLarry);
    assertEquals(0.3, ppPageJimmy, 0.001);
    assertEquals(1.0, ppPageJimmy + ppPageLarry, 0.001);
  }
}