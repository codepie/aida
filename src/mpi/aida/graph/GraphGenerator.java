package mpi.aida.graph;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import mpi.aida.AidaManager;
import mpi.aida.config.AidaConfig;
import mpi.aida.config.settings.DisambiguationSettings;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.data.Mention;
import mpi.aida.data.Mentions;
import mpi.aida.data.PreparedInput;
import mpi.aida.graph.extraction.ExtractGraph;
import mpi.aida.graph.similarity.EnsembleEntityEntitySimilarity;
import mpi.aida.graph.similarity.EnsembleMentionEntitySimilarity;
import mpi.aida.graph.similarity.MaterializedPriorProbability;
import mpi.aida.graph.similarity.util.SimilaritySettings;
import mpi.experiment.trace.GraphTracer;
import mpi.experiment.trace.NullGraphTracer;
import mpi.experiment.trace.Tracer;
import mpi.experiment.trace.data.EntityTracer;
import mpi.experiment.trace.data.MentionTracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphGenerator {
  private static final Logger logger = 
      LoggerFactory.getLogger(GraphGenerator.class);
  
  private PreparedInput content;

  private String docId;

  private SimilaritySettings similaritySettings;

  private SimilaritySettings combSimMeasureSettings;

  private boolean useCoherenceRobustness;

  private double coherenceRobustnessThreshold;

  private double alpha;

  private boolean includeNullEntityCandidates;

  private boolean includeContextMentions;

  private Tracer tracer = null;

  // set this to 20000 for web server so it can still run for web server
  private int maxNumCandidateEntitiesForGraph = 0;

  public GraphGenerator(PreparedInput content, DisambiguationSettings settings, Tracer tracer) {
    //		this.storePath = content.getStoreFile();
    this.content = content;
    this.docId = content.getDocId();
    this.useCoherenceRobustness = settings.shouldUseCoherenceRobustnessTest();
    coherenceRobustnessThreshold = settings.getCohRobustnessThreshold();
    this.similaritySettings = settings.getSimilaritySettings();
    this.combSimMeasureSettings = settings.getCoherenceSimilaritySetting();
    alpha = settings.getAlpha();
    this.includeNullEntityCandidates = settings.isIncludeNullAsEntityCandidate();
    this.includeContextMentions = settings.isIncludeContextMentions();
    this.tracer = tracer;
    try {
      if (AidaConfig.get(AidaConfig.MAX_NUM_CANDIDATE_ENTITIES_FOR_GRAPH) != null) {
        maxNumCandidateEntitiesForGraph = Integer.parseInt(AidaConfig.get(AidaConfig.MAX_NUM_CANDIDATE_ENTITIES_FOR_GRAPH));
      }
    } catch (Exception e) {
      maxNumCandidateEntitiesForGraph = 0;
    }
  }

  public Graph run() throws Exception {
    Graph gData = null;
    gData = generateGraph();
    return gData;
  }

  private Graph generateGraph() throws Exception {
    Mentions mentions = content.getMentions();
    AidaManager.fillInCandidateEntities(docId, mentions, includeNullEntityCandidates, includeContextMentions);
    Set<String> mentionStrings = new HashSet<String>();
    Entities allEntities = new Entities();

    if (includeNullEntityCandidates) {
      allEntities.setIncludesNmeEntities(true);
    }

    // prepare tracing
    for (Mention mention : content.getMentions().getMentions()) {
      MentionTracer mt = new MentionTracer(mention);
      tracer.addMentionForDocId(content.getDocId(), mention, mt);

      for (Entity entity : mention.getCandidateEntities()) {
        EntityTracer et = new EntityTracer(entity.getName());
        tracer.addEntityForMention(mention, entity.getName(), et);
      }
    }

    // gather candidate entities - and prepare tracing
    for (Mention mention : mentions.getMentions()) {
      MentionTracer mt = new MentionTracer(mention);
      tracer.addMentionForDocId(content.getDocId(), mention, mt);

      for (Entity entity : mention.getCandidateEntities()) {
        EntityTracer et = new EntityTracer(entity.getName());
        tracer.addEntityForMention(mention, entity.getName(), et);
      }

      String mentionString = mention.getMention();
      mentionStrings.add(mentionString);
      allEntities.addAll(mention.getCandidateEntities());
    }
    // Safty measure for rmi web server so that it can still run with 40GB of memory 
    if (maxNumCandidateEntitiesForGraph != 0 && allEntities.size() > maxNumCandidateEntitiesForGraph) {
      throw new Exception("Maximum number of candidate entites for graph exceeded " + allEntities.size());
    }

    MaterializedPriorProbability pp = new MaterializedPriorProbability(mentionStrings);

    Set<Mention> mentionsToSolveByLocal = new HashSet<Mention>();

    // IF WE USE ROBUSTNESS, DO THE FOLLOWING
    if (useCoherenceRobustness) {

      double[] l1s = new double[mentions.getMentions().size()];

      EnsembleMentionEntitySimilarity combSimMeasure = new EnsembleMentionEntitySimilarity(content.getMentions(), allEntities, combSimMeasureSettings, content.getDocId(), tracer);

      // for each mention
      int currentMention = 0;
      for (Mention mention : mentions.getMentions()) {
        // get prior distribution
        double[] priorDistribution = calcPriorDistribution(mention, pp);

        // get similarity distribution per UnnormCOMB (IDF+MI)
        double[] simDistribution = calcSimDistribution(mention, combSimMeasure, mentions);

        // get L1 norm of both distributions
        // if the L1 is below the threshold, keep flag mention as
        // SOLVE_BY_LOCAL
        // otherwise, SOLVE_BY_COHERENCE
        double l1 = calcL1(priorDistribution, simDistribution);

        if (l1 < coherenceRobustnessThreshold) {
          mentionsToSolveByLocal.add(mention);
          GraphTracer.gTracer.addMentionToLocalOnly(docId, mention.getMention(), mention.getCharOffset());
        }

        l1s[currentMention] = l1;
        currentMention++;
      }

      if (!(GraphTracer.gTracer instanceof NullGraphTracer)) {
        gatherL1stats(l1s);
        GraphTracer.gTracer.addStat(docId, "Number of fixed mention by coherence robustness", Integer.toString(mentionsToSolveByLocal.size()));
      }
    }

    EnsembleMentionEntitySimilarity mentionEntitySimilarity = new EnsembleMentionEntitySimilarity(content.getMentions(), allEntities, similaritySettings, content.getDocId(), tracer);
    logger.info("Computing the mention-entity similarities...");

    // We might drop entities here, so we have to rebuild the list of unique
    // entities
    allEntities = new Entities();
    if (includeNullEntityCandidates) {
      allEntities.setIncludesNmeEntities(true);
    }
    for (int i = 0; i < mentions.getMentions().size(); i++) {
      Mention currentMention = mentions.getMentions().get(i);
      String mentionText = currentMention.getMention();
      Entities candidateEntities = currentMention.getCandidateEntities();
      for (Entity candidate : candidateEntities) {
        // keyphrase-based mention/entity similarity.
        double similarity = mentionEntitySimilarity.calcSimilarity(currentMention, content.getContext(), candidate);
        double priorProbability = 0.0;
        try {
          priorProbability = pp.getPriorProbability(mentionText, candidate);
          priorProbability = priorProbability * similaritySettings.getPriorWeight();
        } catch (NoSuchElementException e) {
          e.printStackTrace();
        }
        candidate.setMentionEntitySimilarity(similarity);
      }

      if (mentionsToSolveByLocal.contains(currentMention)) {
        // drop all candidates, fix the mention to the one that has the
        // highest
        // local similarity value
        Entity bestCandidate = getBestCandidate(currentMention);

        if (bestCandidate != null) {
          Entities candidates = new Entities();

          candidates.add(bestCandidate);

          currentMention.setCandidateEntities(candidates);

          allEntities.add(bestCandidate);
        }
      } else {
        allEntities.addAll(candidateEntities);
      }
    }
    logger.info("Building the graph...");
    EnsembleEntityEntitySimilarity eeSim = new EnsembleEntityEntitySimilarity(allEntities, similaritySettings, tracer);

    ExtractGraph egraph = new ExtractGraph(docId, mentions, allEntities, eeSim, alpha);
    Graph gData = null;
    try {
      gData = egraph.generateGraph();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return gData;
  }

  private void gatherL1stats(double[] l1s) {
    double l1_total = 0.0;

    for (double l1 : l1s) {
      l1_total += l1;
    }

    double l1_mean = l1_total / l1s.length;

    double varTemp = 0.0;

    for (double l1 : l1s) {
      varTemp += Math.pow(Math.abs(l1_mean - l1), 2);
    }

    double variance = 0;

    if (l1s.length > 1) {
      variance = varTemp / l1s.length - 1;
    }

    GraphTracer.gTracer.addStat(docId, "L1 (prior-sim) Mean", Double.toString(l1_mean));
    GraphTracer.gTracer.addStat(docId, "L1 (prior-sim) Variance", Double.toString(variance));
  }

  private double[] calcPriorDistribution(Mention mention, MaterializedPriorProbability pp) {
    double[] priors = new double[mention.getCandidateEntities().uniqueNameSize()];

    int i = 0;
    for (Entity entity : mention.getCandidateEntities()) {
      priors[i] = pp.getPriorProbability(mention.getMention(), entity);
      i++;
    }

    return priors;
  }

  private double[] calcSimDistribution(Mention mention, EnsembleMentionEntitySimilarity combSimMeasure, Mentions mentions) throws Exception {
    double[] sims = new double[mention.getCandidateEntities().uniqueNameSize()];
    int ide = 0;
    for (Entity e : mention.getCandidateEntities()) {
      sims[ide] = combSimMeasure.calcSimilarity(mention, content.getContext(), e);
      ide++;
    }

    double minSim = Double.POSITIVE_INFINITY;
    double maxSim = Double.NEGATIVE_INFINITY;
    double simSum = 0.0;

    for (double sim : sims) {
      simSum += sim;

      if (sim < minSim) {
        minSim = sim;
      }

      if (sim > maxSim) {
        maxSim = sim;
      }
    }

    // special case
    if (simSum == 0) {
      return sims;
    }

    double[] normSims = new double[sims.length];

    for (int i = 0; i < sims.length; i++) {
      double normSim = sims[i] / simSum;
      normSims[i] = normSim;
    }

    return normSims;
  }

  private double calcL1(double[] priorDistribution, double[] simDistribution) {
    double l1 = 0.0;

    for (int i = 0; i < priorDistribution.length; i++) {
      double diff = Math.abs(priorDistribution[i] - simDistribution[i]);
      l1 += diff;
    }

    assert (l1 >= -0.00001 && l1 <= 2.00001) : "This cannot happen, L1 must be in [0,2]. Was: " + l1;
    return l1;
  }

  private Entity getBestCandidate(Mention m) {
    double bestSim = Double.NEGATIVE_INFINITY;
    Entity bestCandidate = null;

    for (Entity e : m.getCandidateEntities()) {
      if (e.getMentionEntitySimilarity() > bestSim) {
        bestSim = e.getMentionEntitySimilarity();
        bestCandidate = e;
      }
    }

    return bestCandidate;
  }

}
