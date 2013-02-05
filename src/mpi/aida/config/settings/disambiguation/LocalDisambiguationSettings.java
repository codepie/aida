package mpi.aida.config.settings.disambiguation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mpi.aida.config.settings.DisambiguationSettings;
import mpi.aida.config.settings.Settings.TECHNIQUE;
import mpi.aida.graph.similarity.exception.MissingSettingException;
import mpi.aida.graph.similarity.util.SimilaritySettings;

/**
 * Preconfigured settings for the {@see Disambiguator} using only the 
 * mention-entity prior and the keyphrase based similarity.
 */
public class LocalDisambiguationSettings extends DisambiguationSettings {
    
  private static final long serialVersionUID = -1943862223862927646L;

  public LocalDisambiguationSettings() throws MissingSettingException {
    setDisambiguationTechnique(TECHNIQUE.LOCAL);
    
    List<String[]> simConfigs = new LinkedList<String[]>();
    simConfigs.add(new String[] { "UnnormalizedKeyphrasesBasedMISimilarity", "KeyphrasesContext", "1.4616111666431395E-5" });
    simConfigs.add(new String[] { "UnnormalizedKeyphrasesBasedIDFSimilarity", "KeyphrasesContext", "4.291375037765039E-5" });
    simConfigs.add(new String[] { "UnnormalizedKeyphrasesBasedMISimilarity", "KeyphrasesContext", "0.15586170799823845" });
    simConfigs.add(new String[] { "UnnormalizedKeyphrasesBasedIDFSimilarity", "KeyphrasesContext", "0.645200419577534" });   
    
    Map<String, double[]> minMaxs = new HashMap<String, double[]>();
    minMaxs.put("prior", new double[] { 0.0, 1.0} );
    minMaxs.put("UnnormalizedKeyphrasesBasedMISimilarity:KeyphrasesContext", new double[] { 0.0, 840.1373501651881});
    minMaxs.put("UnnormalizedKeyphrasesBasedIDFSimilarity:KeyphrasesContext", new double[] { 0.0, 63207.231647131});
    
    SimilaritySettings switchedKPsettings = new SimilaritySettings(simConfigs, null, 0.19888034256218348, minMaxs);
    switchedKPsettings.setIdentifier("SwitchedKP");
    switchedKPsettings.setPriorThreshold(0.9);
    setSimilaritySettings(switchedKPsettings);
    
    setIncludeNullAsEntityCandidate(false);
  }
}
