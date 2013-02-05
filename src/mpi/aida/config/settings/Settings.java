package mpi.aida.config.settings;

import java.io.Serializable;

/**
 * Combined PreparationSettings and DisambiguationSettings, useful for
 * a calling API.
 */
public class Settings implements Serializable {

  public enum TECHNIQUE {
    LOCAL, LOCAL_ITERATIVE, GRAPH, CHAKRABARTI
  }

  public enum ALGORITHM {
    COCKTAIL_PARTY, COCKTAIL_PARTY_SIZE_CONSTRAINED, RANDOM_WALK
  }
  
  private static final long serialVersionUID = -6602287193597852191L;

  private PreparationSettings preparationSettings = null;

  private DisambiguationSettings disambiguationSettings = null;

  public Settings(PreparationSettings preparationSettings, 
                  DisambiguationSettings disambiguationSettings) {
    this.preparationSettings = preparationSettings;
    this.disambiguationSettings = disambiguationSettings;
  }

  public PreparationSettings getPreparationSettings() {
    return preparationSettings;
  }

  public void setPreparationSettings(PreparationSettings preparationSettings) {
    this.preparationSettings = preparationSettings;
  }

  public DisambiguationSettings getDisambiguationSettings() {
    return disambiguationSettings;
  }

  public void setDisambiguationSettings(DisambiguationSettings disambiguationSettings) {
    this.disambiguationSettings = disambiguationSettings;
  }
}
