package mpi.aida.config.settings;

import java.io.Serializable;

import mpi.aida.preparation.mentionrecognition.FilterMentions.FilterType;

/**
 * Settings for the preparator. Predefined settings are available in
 * {@see mpi.aida.config.settings.preparation}.
 */
public class PreparationSettings implements Serializable {

  private static final long serialVersionUID = -2825720730925914648L;

  private FilterType mentionsFilter = FilterType.Hybrid;

  private String[] filteringTypes = null;
  
  private LANGUAGE language = LANGUAGE.ENGLISH;
  
  public static enum LANGUAGE {
    ENGLISH, GERMAN
  }

  public FilterType getMentionsFilter() {
    return mentionsFilter;
  }

  public void setMentionsFilter(FilterType mentionsFilter) {
    this.mentionsFilter = mentionsFilter;
  }

  public String[] getFilteringTypes() {
    return filteringTypes;
  }

  public void setFilteringTypes(String[] filteringTypes) {
    this.filteringTypes = filteringTypes;
  }
  
  public LANGUAGE getLanguage() {
    return language;
  }
  
  public void setLanguage(LANGUAGE language) {
    this.language = language;
  }
}
