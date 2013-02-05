package mpi.aida.graph.similarity;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mpi.aida.AidaManager;
import mpi.aida.access.DataAccess;
import mpi.aida.config.AidaConfig;
import mpi.aida.data.Context;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.data.Mention;
import mpi.aida.data.Mentions;
import mpi.aida.graph.similarity.EnsembleMentionEntitySimilarity;
import mpi.aida.graph.similarity.util.SimilaritySettings;
import mpi.experiment.trace.NullTracer;
import mpi.experiment.trace.Tracer;

import org.junit.Test;


public class EnsembleMentionEntitySimilarityTest {

  public EnsembleMentionEntitySimilarityTest() {
    AidaConfig.set("dataAccess", "testing");
    AidaManager.init();
  }
  
  @Test
  public void test() throws Exception {
    // All caps PLAYED to check if term expansion is working.
    String text = 
        "When Page PLAYED Kashmir at Knebworth , his Les Paul was uniquely tuned .";
    
    Context context = new Context(Arrays.asList(text.split(" ")));
    
    String n1 = "Kashmir";
    String n2 = "Kashmir_(song)";
    String n3 = "Jimmy_Page";
    
    Entity e1 = new Entity(n1, DataAccess.getIdForYagoEntityId(n1));
    Entity e2 = new Entity(n2, DataAccess.getIdForYagoEntityId(n2));
    Entity e3 = new Entity(n3, DataAccess.getIdForYagoEntityId(n3));

    Entities entities = new Entities();
    entities.add(e1);
    entities.add(e2);
    entities.add(e3);
    
    Tracer tracer = new NullTracer(); 
    
    List<String[]> simConfigs = new LinkedList<String[]>();
    simConfigs.add(new String[] { "UnnormalizedKeyphrasesBasedMISimilarity", "KeyphrasesContext", "0.95" });
    simConfigs.add(new String[] { "UnnormalizedKeyphrasesBasedIDFSimilarity", "KeyphrasesContext", "0.05" });
    simConfigs.add(new String[] { "UnnormalizedKeyphrasesBasedMISimilarity", "KeyphrasesContext", "0.475" });
    simConfigs.add(new String[] { "UnnormalizedKeyphrasesBasedIDFSimilarity", "KeyphrasesContext", "0.025" });

    List<String[]> eeSimConfigs = new LinkedList<String[]>();

    eeSimConfigs.add(new String[] { "MilneWittenEntityEntitySimilarity", "1.0" });

    double priorWeight = 0.5;
    
    Map<String, double[]> maxMinSettings = new HashMap<String, double[]>();
    maxMinSettings.put(
        "UnnormalizedKeyphrasesBasedIDFSimilarity:KeyphrasesContext",
        new double[] { 0, 70000 });
    maxMinSettings.put(
        "UnnormalizedKeyphrasesBasedMISimilarity:KeyphrasesContext",
        new double[] { 0, 1000 });
    maxMinSettings.put("prior", new double[] { 0.0, 1.0 });

    SimilaritySettings settings = 
        new SimilaritySettings(
            simConfigs, eeSimConfigs, priorWeight, maxMinSettings);
    settings.setPriorThreshold(0.8);

    Mentions ms = new Mentions();
    Mention m1 = new Mention();
    m1.setMention("Page");
    m1.setStartToken(1);
    m1.setEndToken(1);
    ms.addMention(m1);
    Mention m2 = new Mention();
    m2.setMention("Kashmir");
    m2.setStartToken(3);
    m2.setEndToken(3);
    ms.addMention(m2);
    AidaManager.fillInCandidateEntities(ms);

    EnsembleMentionEntitySimilarity emes = new EnsembleMentionEntitySimilarity(ms, entities, settings, tracer);

    double simPage = emes.calcSimilarity(m1, context, e3);
    double simKashmir = emes.calcSimilarity(m2, context, e2);
    
    assertEquals(0.000044195, simPage, 0.000000001);
    assertEquals(0.050000, simKashmir, 0.00001);
  }
}
