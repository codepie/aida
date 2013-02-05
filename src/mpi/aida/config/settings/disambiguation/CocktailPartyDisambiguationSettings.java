package mpi.aida.config.settings.disambiguation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mpi.aida.config.settings.DisambiguationSettings;
import mpi.aida.config.settings.Settings.ALGORITHM;
import mpi.aida.config.settings.Settings.TECHNIQUE;
import mpi.aida.graph.similarity.exception.MissingSettingException;
import mpi.aida.graph.similarity.util.SimilaritySettings;
import mpi.experiment.trace.GraphTracer.TracingTarget;

/**
 * Preconfigured settings for the {@see Disambiguator} using the mention-entity
 * prior, the keyphrase based similarity, and the MilneWitten Wikipedia link
 * based entity coherence.
 * 
 * This gives the best quality and should be used in comparing results against
 * AIDA. 
 */
public class CocktailPartyDisambiguationSettings extends DisambiguationSettings {
    
  private static final long serialVersionUID = 5867674989478781057L;

  public CocktailPartyDisambiguationSettings() throws MissingSettingException {
    setAlpha(0.6);
    setTracingTarget(TracingTarget.WEB_INTERFACE);
     
    setDisambiguationTechnique(TECHNIQUE.GRAPH);
    setDisambiguationAlgorithm(ALGORITHM.COCKTAIL_PARTY_SIZE_CONSTRAINED);
    setUseExhaustiveSearch(true);
    setUseNormalizedObjective(true);
    setEntitiesPerMentionConstraint(5);
    setUseCoherenceRobustnessTest(true);
    setCohRobustnessThreshold(0.9);
    
    Map<String, double[]> minMaxs = new HashMap<String, double[]>();
    minMaxs.put("prior", new double[] { 0.0, 1.0} );
    minMaxs.put("UnnormalizedKeyphrasesBasedMISimilarity:KeyphrasesContext", new double[] { 0.0, 840.1373501651881});
    minMaxs.put("UnnormalizedKeyphrasesBasedIDFSimilarity:KeyphrasesContext", new double[] { 0.0, 63207.231647131});
    
    List<String[]> simConfigs = new LinkedList<String[]>();
    simConfigs.add(new String[] { "UnnormalizedKeyphrasesBasedMISimilarity", "KeyphrasesContext", "1.4616111666431395E-5" });
    simConfigs.add(new String[] { "UnnormalizedKeyphrasesBasedIDFSimilarity", "KeyphrasesContext", "4.291375037765039E-5" });
    simConfigs.add(new String[] { "UnnormalizedKeyphrasesBasedMISimilarity", "KeyphrasesContext", "0.15586170799823845" });
    simConfigs.add(new String[] { "UnnormalizedKeyphrasesBasedIDFSimilarity", "KeyphrasesContext", "0.645200419577534" }); 
    List<String[]> cohConfigs = new LinkedList<String[]>();
    cohConfigs.add(new String[] { "MilneWittenEntityEntitySimilarity", "1.0" });

    SimilaritySettings switchedKPsettings = new SimilaritySettings(simConfigs, cohConfigs, 0.19888034256218348, minMaxs);
    switchedKPsettings.setIdentifier("SwitchedKP");
    switchedKPsettings.setPriorThreshold(0.9);
    setSimilaritySettings(switchedKPsettings);
    
    simConfigs = new LinkedList<String[]>();
    simConfigs.add(new String[] { "UnnormalizedKeyphrasesBasedMISimilarity", "KeyphrasesContext", "0.971742997195044" });
    simConfigs.add(new String[] { "UnnormalizedKeyphrasesBasedIDFSimilarity", "KeyphrasesContext", "0.028257002804955994" });
    SimilaritySettings unnormalizedKPsettings = new SimilaritySettings(simConfigs, null, 0.0, minMaxs);
    switchedKPsettings.setIdentifier("CoherenceRobustnessTest");
    setCoherenceSimilaritySetting(unnormalizedKPsettings);
  }
}