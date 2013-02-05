package mpi.aida.graph.similarity.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import mpi.aida.AidaManager;
import mpi.aida.config.AidaConfig;
import mpi.aida.data.Context;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.data.Mention;
import mpi.aida.graph.similarity.context.KeyphrasesContext;
import mpi.aida.graph.similarity.measure.UnnormalizedKeyphrasesBasedMISimilarity;
import mpi.experiment.trace.NullTracer;
import mpi.experiment.trace.Tracer;

import org.junit.Test;

public class KeyphrasesBasedSimilarityTest {

  public KeyphrasesBasedSimilarityTest() {
    AidaConfig.set("dataAccess", "testing");
    AidaManager.init();
  }
  
  @Test
  public void testCalcMISimilarity() throws Exception {
    // All caps PLAYED to check if term expansion is working.
    String text = 
        "When Page played at Knebworth , his Les Paul was uniquely tuned .";
    
    Context context = new Context(Arrays.asList(text.split(" ")));
    
    String n1 = "Kashmir";
    String n2 = "Kashmir_(song)";
    String n3 = "Jimmy_Page";
    
    Entity e1 = AidaManager.getEntity(n1);
    Entity e2 = AidaManager.getEntity(n2);
    Entity e3 = AidaManager.getEntity(n3);

    Entities entities = new Entities();
    entities.add(e1);
    entities.add(e2);
    entities.add(e3);
    
    Tracer tracer = new NullTracer();
    KeyphrasesContext kpContext = new KeyphrasesContext(entities);
    UnnormalizedKeyphrasesBasedMISimilarity kpSimilarity = 
        new UnnormalizedKeyphrasesBasedMISimilarity(tracer);
    double sim1 = 
        kpSimilarity.calcSimilarity(new Mention(), context, e1, kpContext);
    double sim2 = 
        kpSimilarity.calcSimilarity(new Mention(), context, e2, kpContext);
    double sim3 = 
        kpSimilarity.calcSimilarity(new Mention(), context, e3, kpContext);

    assertTrue(sim1 < sim2);

    assertEquals(0.0, sim1, 0.000000001); 
    assertEquals(0.00001517, sim2, 0.00000001);
    assertEquals(0.00009718, sim3, 0.00000001);
  }
  
  @Test
  public void testCapsMatch() throws Exception {
    // All caps PLAYED to check if term expansion is working.
    String text = 
        "WHEN Page PLAYED AT Knebworth , HIS Les Paul WAS UNIQUELY TUNED .";
    
    Context context = new Context(Arrays.asList(text.split(" ")));
    
    String n1 = "Kashmir";
    String n2 = "Kashmir_(song)";
    String n3 = "Jimmy_Page";
    
    Entity e1 = AidaManager.getEntity(n1);
    Entity e2 = AidaManager.getEntity(n2);
    Entity e3 = AidaManager.getEntity(n3);

    Entities entities = new Entities();
    entities.add(e1);
    entities.add(e2);
    entities.add(e3);
    
    Tracer tracer = new NullTracer();
    KeyphrasesContext kpContext = new KeyphrasesContext(entities);
    UnnormalizedKeyphrasesBasedMISimilarity kpSimilarity = 
        new UnnormalizedKeyphrasesBasedMISimilarity(tracer);
    double sim1 = 
        kpSimilarity.calcSimilarity(new Mention(), context, e1, kpContext);
    double sim2 = 
        kpSimilarity.calcSimilarity(new Mention(), context, e2, kpContext);
    double sim3 = 
        kpSimilarity.calcSimilarity(new Mention(), context, e3, kpContext);

    assertTrue(sim1 < sim2);

    assertEquals(0.0, sim1, 0.000000001); 
    assertEquals(0.00001517, sim2, 0.00000001);
    assertEquals(0.00009718, sim3, 0.00000001);
  }
  
  @Test
  public void testSimilarityNoMentionMatch() throws Exception {
    // All caps PLAYED to check if term expansion is working.
    String text = 
        "When Page played Kashmir at Knebworth , his Les Paul was uniquely tuned .";
    
    Context context = new Context(Arrays.asList(text.split(" ")));
    
    String n1 = "Nomatching_Page";
    Entity e1 = AidaManager.getEntity(n1);
    Entities entities = new Entities();
    entities.add(e1);
    
    Tracer tracer = new NullTracer();
    KeyphrasesContext kpContext = new KeyphrasesContext(entities);
    UnnormalizedKeyphrasesBasedMISimilarity kpSimilarity = 
        new UnnormalizedKeyphrasesBasedMISimilarity(tracer);
    Mention pageMention = new Mention();
    pageMention.setStartToken(1);
    pageMention.setEndToken(1);
    double sim1 = 
        kpSimilarity.calcSimilarity(pageMention, context, e1, kpContext);

    assertEquals(0.0, sim1, 0.00000000001);
  }
  
  @Test
  public void testSimilarityNoStopwordMatch() throws Exception {
    // All caps PLAYED to check if term expansion is working.
    String text = 
        "Page played and the crowd went wild .";
    
    Context context = new Context(Arrays.asList(text.split(" ")));
    
    String n1 = "Stopword_Page";
    Entity e1 = AidaManager.getEntity(n1);
    Entities entities = new Entities();
    entities.add(e1);
    
    Tracer tracer = new NullTracer();
    KeyphrasesContext kpContext = new KeyphrasesContext(entities);
    UnnormalizedKeyphrasesBasedMISimilarity kpSimilarity = 
        new UnnormalizedKeyphrasesBasedMISimilarity(tracer);
    Mention pageMention = new Mention();
    pageMention.setStartToken(0);
    pageMention.setEndToken(0);
    double sim1 = 
        kpSimilarity.calcSimilarity(pageMention, context, e1, kpContext);

    assertEquals(0.0, sim1, 0.00000000001);
  }
}
