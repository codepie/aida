package mpi.aida.config.settings.preparation;

import mpi.aida.config.settings.PreparationSettings;
import mpi.aida.preparation.mentionrecognition.FilterMentions.FilterType;

/**
 * Preparator setting that tokenizes the input text using the 
 * Stanford CoreNLP tokenizer. Mentions need to be marked up with square
 * bracktets. E.g.:
 * [[Einstein]] was born in [[Ulm]].
 */
public class StanfordManualPreparationSettings extends PreparationSettings {

  private static final long serialVersionUID = 3743560957961384100L;

  public StanfordManualPreparationSettings() {
    this.setMentionsFilter(FilterType.Manual);
    this.setFilteringTypes(null);
  }
}
