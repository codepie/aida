package mpi.aida.config.settings.disambiguation;

import java.util.HashMap;
import java.util.Map;

import mpi.aida.config.settings.DisambiguationSettings;
import mpi.aida.config.settings.Settings.TECHNIQUE;
import mpi.aida.graph.similarity.exception.MissingSettingException;
import mpi.aida.graph.similarity.util.SimilaritySettings;

/**
 * Preconfigured settings for the {@see Disambiguator} using only the 
 * mention-entity prior.
 */
public class PriorOnlyDisambiguationSettings extends DisambiguationSettings {
    
  private static final long serialVersionUID = 2212272023159361340L;

  public PriorOnlyDisambiguationSettings() throws MissingSettingException {
    setDisambiguationTechnique(TECHNIQUE.LOCAL);
    
    Map<String, double[]> minMaxs = new HashMap<String, double[]>();
    minMaxs.put("prior", new double[] { 0.0, 1.0} );
    
    SimilaritySettings priorSettings = new SimilaritySettings(null, null, 1.0, minMaxs);
    priorSettings.setIdentifier("Prior");
    setSimilaritySettings(priorSettings);
    
    setIncludeNullAsEntityCandidate(false);
  }
}


