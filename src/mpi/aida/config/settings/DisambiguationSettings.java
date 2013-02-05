package mpi.aida.config.settings;

import java.io.Serializable;

import mpi.aida.graph.similarity.util.SimilaritySettings;
import mpi.experiment.trace.GraphTracer.TracingTarget;

/**
 * Settings for the Disambigutor. Configures the disambiguation process.
 * Pre-configured settings are available in the 
 * {@see mpi.aida.config.settings.disambiguation} package.
 */
public class DisambiguationSettings implements Serializable {

  private static final long serialVersionUID = 1210739181527236465L;

  /** 
   * Balances the mention-entity edge weights (alpha) 
   * and the entity-entity edge weights (1-alpha)
   */
  private double alpha = 0;

  /**
   * Technique to solve the disambiguation graph with. Most commonly this
   * is LOCAL for mention-entity similarity edges only and 
   * GRAPH to include the entity coherence.
   */
  private Settings.TECHNIQUE disambiguationTechnique = null;

  /**
   * If TECHNIQUE.GRAPH is chosen above, this specifies the algorithm to
   * solve the disambiguation graph. Can be COCKTAIL_PARTY for the full
   * disambiguation graph, COCKTAIL_PARTY_SIZE_CONSTRAINED for a heuristically
   * pruned graph, and RANDOM_WALK for using a PageRank style random walk
   * to find the best entity.
   */
  private Settings.ALGORITHM disambiguationAlgorithm = null;

  /**
   * Set to true to use exhaustive search in the final solving stage of
   * ALGORITHM.COCKTAIL_PARTY. Set to false to do a hill-climbing search
   * from a random starting point.
   */
  private boolean useExhaustiveSearch = false;

  /**
   * Set to true to normalize the minimum weighted degree in the 
   * ALGORITHM.COCKTAIL_PARTY by the number of graph nodes. This prefers
   * smaller solutions.
   */
  private boolean useNormalizedObjective = false;

  /**
   * Settings to compute the edge-weights of the disambiguation graph.
   */
  private SimilaritySettings similaritySettings = null;
  
  /**
   * Settings to compute the initial mention-entity edge weights when
   * using coherence robustness.
   */
  private SimilaritySettings coherenceSimilaritySetting = null;

  /**
   * Number of candidates to keep for for 
   * ALGORITHM.COCKTAIL_PARTY_SIZE_CONSTRAINED.
   */
  private int entitiesPerMentionConstraint = 5;
  
  /**
   * Set to true to enable the coherence robustness test, fixing mentions
   * with highly similar prior and similarity distribution to the most
   * promising candidate before running the graph algorithm.
   */
  private boolean useCoherenceRobustnessTest = true;

  /**
   * Threshold of the robustness test, below which the the L1-norm between
   * prior and sim results in the fixing of the entity candidate.
   */
  private double cohRobustnessThreshold = 0;
  
  private double nullMappingThresholdFactor = -1.0;
  
  private boolean includeNullAsEntityCandidate = false;
  
  private boolean includeContextMentions = false;
  
  private String storeFile = null;

  private TracingTarget tracingTarget = null;
  
  public void setStoreFile(String path) {
    this.storeFile = path;
  }

  public String getStoreFile() {
    return storeFile;
  }

  public void setTracingTarget(TracingTarget tracingTarget) {
    this.tracingTarget = tracingTarget;
  }

  public TracingTarget getTracingTarget() {
    return tracingTarget;
  }

  public Settings.TECHNIQUE getDisambiguationTechnique() {
    return disambiguationTechnique;
  }

  public void setDisambiguationTechnique(Settings.TECHNIQUE disambiguationTechnique) {
    this.disambiguationTechnique = disambiguationTechnique;
  }

  public Settings.ALGORITHM getDisambiguationAlgorithm() {
    return disambiguationAlgorithm;
  }

  public void setDisambiguationAlgorithm(Settings.ALGORITHM disambiguationAlgorithm) {
    this.disambiguationAlgorithm = disambiguationAlgorithm;
  }

  public boolean shouldUseExhaustiveSearch() {
    return useExhaustiveSearch;
  }

  public void setUseExhaustiveSearch(boolean useExhaustiveSearch) {
    this.useExhaustiveSearch = useExhaustiveSearch;
  }

  public double getAlpha() {
    return alpha;
  }

  public void setAlpha(double alpha) {
    this.alpha = alpha;
  }

  public SimilaritySettings getSimilaritySettings() {
    return similaritySettings;
  }

  public void setSimilaritySettings(SimilaritySettings similaritySettings) {
    this.similaritySettings = similaritySettings;
  }

  public SimilaritySettings getCoherenceSimilaritySetting() {
    return coherenceSimilaritySetting;
  }

  public void setCoherenceSimilaritySetting(SimilaritySettings similaritySettings) {
    this.coherenceSimilaritySetting = similaritySettings;
  }

  public int getEntitiesPerMentionConstraint() {
    return entitiesPerMentionConstraint;
  }

  public void setEntitiesPerMentionConstraint(int entitiesPerMentionConstraint) {
    this.entitiesPerMentionConstraint = entitiesPerMentionConstraint;
  }

  public double getCohRobustnessThreshold() {
    return cohRobustnessThreshold;
  }

  public void setCohRobustnessThreshold(double cohRobustnessThreshold) {
    this.cohRobustnessThreshold = cohRobustnessThreshold;
  }

  public boolean shouldUseNormalizedObjective() {
    return useNormalizedObjective;
  }

  public void setUseNormalizedObjective(boolean useNormalizedObjective) {
    this.useNormalizedObjective = useNormalizedObjective;
  }
  
  public boolean shouldUseCoherenceRobustnessTest() {
    return useCoherenceRobustnessTest;
  }

  public void setUseCoherenceRobustnessTest(boolean useCoherenceRobustnessTest) {
    this.useCoherenceRobustnessTest = useCoherenceRobustnessTest;
  }

  public double getNullMappingThresholdFactor() {
    return nullMappingThresholdFactor;
  }
  
  public void setNullMappingThresholdFactor(double nullMappingThresholdFactor) {
    this.nullMappingThresholdFactor = nullMappingThresholdFactor;
  }

  public boolean isIncludeNullAsEntityCandidate() {
    return includeNullAsEntityCandidate;
  }

  public void setIncludeNullAsEntityCandidate(boolean includeNullAsEntityCandidate) {
    this.includeNullAsEntityCandidate = includeNullAsEntityCandidate;
  }

  public void setIncludeContextMentions(boolean flag) {
    this.includeContextMentions = flag;
  }
  
  public boolean isIncludeContextMentions() {
    return includeContextMentions;
  }
}
