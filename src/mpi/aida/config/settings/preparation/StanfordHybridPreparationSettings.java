package mpi.aida.config.settings.preparation;

import mpi.aida.config.settings.PreparationSettings;
import mpi.aida.preparation.mentionrecognition.FilterMentions.FilterType;

/**
 * Preparator setting that tokenizes the input text using the 
 * Stanford CoreNLP tokenizer. Mentions are recognized using the 'ner'
 * stage of the CoreNLP pipeline. In additon, they can be marked up 
 * explicitly by square brackets, e.g.:
 * [[Einstein]] was born in [[Ulm]].
 */
public class StanfordHybridPreparationSettings extends PreparationSettings {

  private static final long serialVersionUID = 3743560957961384100L;

  public StanfordHybridPreparationSettings() {
    this.setMentionsFilter(FilterType.Hybrid);
    this.setFilteringTypes(null);
  }
}
