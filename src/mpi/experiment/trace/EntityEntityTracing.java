package mpi.experiment.trace;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mpi.aida.data.Entity;
import mpi.aida.data.Mention;
import mpi.aida.util.CollectionUtils;
import mpi.experiment.measure.EvaluationMeasures;
import mpi.experiment.trace.measures.MeasureTracer;

public class EntityEntityTracing { 

  private String mainEntity;
  private Map<String, Integer> correctRanking;
   
  
  private Map<String, Map<String, MeasureTracer>> entityEntityMeasureTracer = new HashMap<String, Map<String, MeasureTracer>>();

  // below is document EE specific
  private boolean doDocumentEETracing;

  private Map<String, String> mention2correctEntity;
  private Map<Mention, Set<String>> mention2candidates;
  private Collection<String> correctEntities;
  
  
  private int totalRanks = 0;
  private double totalReciprocalRanks = 0.0;

  private Map<String, Double> weightedDegress;
  
  
  private String html = null;
  
  private DecimalFormat df = new DecimalFormat("0.0E0");

  private Map<String, Map<String, Double>> entityContext = new HashMap<String, Map<String,Double>>();

  
  public EntityEntityTracing() {
    this.doDocumentEETracing = false;
  }
  
  public EntityEntityTracing(boolean doDocumentEETracing) {
    this.doDocumentEETracing = doDocumentEETracing;
  }
  
  public String getHtml() {
    // make sure the html is generated, but don't generate twice    
    if (html == null) {
      generateHtml(); 
    }
    
    return html;
  }
  
  /**
   * Call this method to generate/store tracer output in <em>html</em> and free all other stuff.
   * This uses a lot less memory and is necessary to have tracing for multiple documents.
   */
  public void generateHtml() {
    html = generateOutput();
    
    entityEntityMeasureTracer = null;
    mention2correctEntity = null;
    mention2candidates = null;
    weightedDegress = null;
  }

  public String generateOutput() {
    StringBuilder sb = new StringBuilder();
    
    Set<String> entities = getAllEntities();
    List<String> sortedEntities = new ArrayList<String>(entities.size());
    sortedEntities.addAll(entities);
    Collections.sort(sortedEntities, new Comparator<String>() {

      @Override
      public int compare(String te1, String te2) {
        return te1.compareTo(te2);
      }
    });
    
    if (doDocumentEETracing) {
      List<Mention> sortedMentions = new ArrayList<Mention>(mention2candidates.keySet());
      Collections.sort(sortedMentions);

      sb.append("<h1>Mentions</h1>");
      sb.append("<table class='mmTable'>").append(
          "<td style='font-size:small'><strong>right:</strong> connected mentions<br /><strong>down</strong>: mentions with correct entity</td>");
      for (Mention m : sortedMentions) {
        String mTitle = "<strong>" + m.getMention() + "</strong>/" + m.getCharOffset();
        sb.append("<td>").append(mTitle).append("</td>");
      }

      for (Mention m1 : sortedMentions) {
        String m1Title = "<strong>" + m1.getMention() + "</strong>/" + m1.getCharOffset() + " ("
            + mention2correctEntity.get(m1.getIdentifiedRepresentation()) + ")";
        sb.append("<tr><td>").append(m1Title).append("</td>");
        for (Mention m2 : sortedMentions) {
          sb.append(getMentionMentionEntities(m1, m2));
        }
        sb.append("</tr>");
      }
      sb.append("</table>");

      sb.append("<h2>Scores</h2>");

      double mrr = totalReciprocalRanks / (double) totalRanks;

      sb.append("<ul>");
      sb.append("<li>Mean Reciprocal Rank: " + mrr + "</li>");
      sb.append("</ul>");

      sb.append("<h1>Entities ordered by Weighted Degree</h1><ol>");
      for (Entry<String, Double> e : weightedDegress.entrySet()) {
        String color = (correctEntities.contains(e.getKey())) ? "#ADFF2F" : "#FFA500";
        sb.append("<li style='background-color:").append(color).append(";'>").append(e.getKey()).append(": ").append(e.getValue()).append("</li>");
      }
      sb.append("</ol>");
    }
    
    sb.append("<p><a href='#sims'>sims</a> - <a href='#overview'>overview</a></p>");
    
    sb.append("<h1><a name='sims'>Entity-Entity Similarities");
    if (mainEntity != null) {
      sb.append(": ").append(mainEntity);
    }
    sb.append("</a></h1>");
    
    if (doDocumentEETracing) {
      sb.append("<a name='eesims'></a>");
      for (String entity : sortedEntities) {
        if (doDocumentEETracing && !correctEntities.contains(entity)) {
          continue;
        }
        sb.append("<a href='#" + entity + "'>" + entity + "</a>, ");
      }
    }
    
    sb.append("<table style='width=300px;padding:10px'>");
    for (String entity : sortedEntities) {
      if (doDocumentEETracing && !correctEntities.contains(entity)) {
        continue;
      } else if (!doDocumentEETracing && !mainEntity.equals(entity)) {
        continue;
      }
      sb.append(getEntityDetailedOutput(entity));
    }
    sb.append("</table>");
    
    if (getEntityContext(mainEntity) != null) {
      sb.append(getEntityOverviewOutput(mainEntity));
    }
    
    return sb.toString();
  }
  
  private String getEntityOverviewOutput(String main) {
    StringBuilder sb = new StringBuilder();
    
    sb.append("<h1><a name='overview'>Overview: ").append(main).append("</a></h1>");
    sb.append("<table border='1'><tr><th width='30%'>Keyphrase (Weight)</th><th width='55%'>List of Matching Entities (Details)</th><th>Source</th></tr>"); 
    
    // get all keyphrases/words for 
    Map<String, Double> sortedK = CollectionUtils.sortMapByValue(getEntityContext(main), true);
    for (String k : sortedK.keySet()) {
      sb.append("<tr>");
      sb.append("<td>").append("<strong>").append(k).append("</strong>").append(" (").append(df.format(sortedK.get(k))).append(") </td>");
      
      // find all matching entities + keywords
      Map<String, Double> matchingEntities = new HashMap<String, Double>();
      
      for (String other : entityContext.keySet()) {
        if (other.equals(main)) continue;
        
        if (entityContext.get(other).containsKey(k)) {
          matchingEntities.put(other, entityContext.get(other).get(k));
        }
      }
      
      Map<String, Double> sortedMatches = CollectionUtils.sortMapByValue(matchingEntities, true);
      sb.append("<td>");
      for (String e : sortedMatches.keySet()) {
        sb.append("<strong>").append(e).append("</strong>").append(" (").append(df.format(sortedMatches.get(e))).append(") - ");
      }
      sb.append("</td>");
      
//      sb.append("<td>"+DataAccess.getKeyphraseSource(main, k)+"</td></tr>");
    }
    
    sb.append("</table>");    
    return sb.toString();
  }

  private String getMentionMentionEntities(Mention m1, Mention m2) {
    if (m1.equals(m2)) {
      return "<td><span style='background-color:black;color:white'>same_mention</span></td>";
    }
    
    String e1 = mention2correctEntity.get(m1.getIdentifiedRepresentation());    
    String e2 = mention2correctEntity.get(m2.getIdentifiedRepresentation());
    
    if (e1.equals(Entity.NO_MATCHING_ENTITY) || e2.equals(Entity.NO_MATCHING_ENTITY)) {
      return "<td>no_entity</td>";
    } else if (e1.equals(e2)) {
      return "<td>same_entity</td>";
    }
    
    Map<String, Double> connectedEntities = new HashMap<String, Double>();
    
    if (entityEntityMeasureTracer.containsKey(e1)) {
      for (Entry<String, MeasureTracer> e : entityEntityMeasureTracer.get(e1).entrySet()) {
        // only add those mentions that are candidates of the mention
        if (mention2candidates.get(m2).contains(e.getKey())) {
          connectedEntities.put(e.getKey(), e.getValue().getScore());
        }
      }       
    }
    
    Map<String, Double> sortedNeighbors = CollectionUtils.sortMapByValue(connectedEntities, true);
       
    StringBuilder sb = new StringBuilder();
    
    int positionCorrect = Integer.MAX_VALUE;
    int i = 0;
    int maxEntities = 3;
    
    for (String entityName : sortedNeighbors.keySet()) {
      i++;
      
      if (entityName.equals(e2)) {
        positionCorrect = i;

        // do not draw everything
        if (!(i > Math.min(maxEntities, correctEntities.size()))) {
          sb.append("<strong>").append(entityName).append(": ").append(df.format(sortedNeighbors.get(entityName))).append("</strong>").append("<br />");
        }
      } else if (!(i > Math.min(maxEntities, correctEntities.size()))) {
        sb.append(entityName).append(": ").append(df.format(sortedNeighbors.get(entityName))).append("<br />");
      }
    }
    
    if (positionCorrect > maxEntities) {
      sb.append("...<br />");
      Double value = sortedNeighbors.get(e2);
      if (value == null) {
        value = Double.NaN;
      }
      sb.append("<strong>").append(positionCorrect).append(". ").append(e2).append(": ").append(df.format(value)).append("<strong>").append("<br />");
    }
               
    totalRanks++;
    
    double rr = 0.0;
    
    if (positionCorrect != Integer.MAX_VALUE) {
      rr = (double) 1 / (double) (positionCorrect);
      totalReciprocalRanks += rr;
    }
        
    String color = calcRankColor(positionCorrect);
    
    String cell = "<td style='background-color:" + color + ";'>" + sb.toString() + "</td>";
    
    return cell;
  }

  private Set<String> getAllEntities() {
    return entityEntityMeasureTracer.keySet();
  }

  public synchronized void addEntityEntityMeasureTracer(String e1, String e2, MeasureTracer mt) {
    Map<String, MeasureTracer> second = entityEntityMeasureTracer.get(e1);
    
    if (second == null) {
      second = new HashMap<String, MeasureTracer>();
      entityEntityMeasureTracer.put(e1, second);
    }
    
    second.put(e2, mt);
  }

//  public void addEntityContextTracer(String entity, TracerPart mt) {
//    entityContextTracers.put(entity, mt);
//  }

  private String getEntityDetailedOutput(String entity) {
    StringBuilder sb = new StringBuilder();
    sb.append("<tr>");
        
    // entity itself
    sb.append("<td style='vertical-align:top;border-bottom:1px solid gray;border-right:1px solid gray;'>")
      .append("<strong><a name='").append(entity).append("'>")
      .append(entity).append("</a></strong><br />");
    
//    if (entityContextTracers.get(entity.entity) != null) {
//      sb.append(entityContextTracers.get(entity.entity).getOutput());
//    }
    sb.append("</td>");
    
    Map<String, Double> connectedEntities = new HashMap<String, Double>();
    for (Entry<String, MeasureTracer> e : entityEntityMeasureTracer.get(entity).entrySet()) {
      connectedEntities.put(e.getKey(), e.getValue().getScore());
    }       
    Map<String, Double> sortedNeighbors = CollectionUtils.sortMapByValue(connectedEntities, true);
    
    // all correct entities
    sb.append("<td style='vertical-align:top;border-bottom:1px solid gray;border-right:1px solid gray'>");
    sb.append("<div style='width:90%;padding:5px;color:#CCCCCC;text-align:right'><em>Related entities</em></div>");
            
    List<List<String>> rankedNeighbors = convertToRanks(sortedNeighbors);
    Map<String, Double> neighbor2rank = EvaluationMeasures.convertToAverageRanks(rankedNeighbors);
    Map<String, Double> sortedNeighbor2rank = CollectionUtils.sortMapByValue(neighbor2rank, false);
    
    for (String entityName : sortedNeighbor2rank.keySet()) {
      if (doDocumentEETracing && !correctEntities.contains(entityName)) {
        continue;
      }
      
      double actualRank = neighbor2rank.get(entityName); // average can be 0.5
      
      sb.append("<span style='font-size:14pt; font-weight:bold;'>");
      if (!doDocumentEETracing) {
        int correctRank = correctRanking.get(entityName);
        double diff = actualRank-correctRank;
        String color = calcRelDistColor(1-(double)Math.abs(diff)/(double)sortedNeighbors.size());
        sb.append("<span style='background-color:"+color+";'>").append(actualRank+". ("+correctRank+"|"+diff+")</span> ");
      }
      sb.append(entityName).append(":</span> <span style='font-size:14pt; font-style:italic;'>").append(sortedNeighbors.get(entityName)).append("</span>").append("<br />");
                  
      MeasureTracer targetMt = getEntityEntityMeasureTracer(entity, entityName);
      if (targetMt != null) {
        String mtOutput = targetMt.getOutput();
        sb.append(mtOutput).append("<br /><br />\n");
      }
    }
    sb.append("</td>");
    
    // top connected entities 
    if (doDocumentEETracing) {
      sb.append("<td style='vertical-align:top;border-bottom:1px solid gray;'>");
      sb.append("<div style='width:90%;padding:5px;color:#CCCCCC;text-align:right'><em>Best Entities</em></div>");

      int i = 0;

      for (String entityName : sortedNeighbors.keySet()) {
        sb.append("<strong>").append(entityName).append(": ").append(sortedNeighbors.get(entityName)).append("</strong>").append("<br />");

        MeasureTracer targetMt = getEntityEntityMeasureTracer(entity, entityName);
        if (targetMt != null) {
          String mtOutput = targetMt.getOutput();
          sb.append(mtOutput).append("<br /><br />");
        }

        if (++i > Math.max(5, correctEntities.size())) {
          break;
        }
      }
      sb.append("</td>"); 
    }
    sb.append("</tr>");    
    
    return sb.toString();
  }
  
  private List<List<String>> convertToRanks(Map<String, Double> sortedNeighbors) {
    List<List<String>> rankedNeighbors = new LinkedList<List<String>>();    
    
    List<String> currentRank = null;
    double currentValue = -1.0;

    for (Entry<String, Double> e : sortedNeighbors.entrySet()) {
      String rankedEntity = e.getKey();
      double rankValue = e.getValue();

      if (rankValue == currentValue) {
        currentRank.add(rankedEntity);
      } else {
        currentRank = new LinkedList<String>();
        currentRank.add(rankedEntity);
        rankedNeighbors.add(currentRank);
        currentValue = rankValue;
      }
    }
      
    return rankedNeighbors;
  }
  
  private MeasureTracer getEntityEntityMeasureTracer(String e1, String e2) {
    String first = e1;
    String second = e2;
    

    Map<String, MeasureTracer> two = entityEntityMeasureTracer.get(first);
    
    if (two != null) {
      return two.get(second);
    } else {
      return null;
    }
  }
  
  public void setMention2Candidates(Map<Mention, Set<String>> m2c) {
    this.mention2candidates = m2c;
  }
  
  public void setMention2CorrectEntity(Map<String, String> m2ce) {
    this.mention2correctEntity = m2ce;
  }
  
  public void setCorrectEntities(Collection<String> correctEntities) {
    this.correctEntities = correctEntities;
  }
  
  private String calcRankColor(int rank) {
    String color = "#FFFFFF";
    
    if (rank > 3) {
      color = "#FF0000";
    }
    
    switch (rank) {
      case 1:
        color = "#00FF00";
        break;
      case 2:
        color = "#ADFF2F";
        break;
      case 3:
        color = "#FFA500";
        break;
    }
    
    return color;
  }
  
  private String calcRelDistColor(double test) {
    String color = "#FFFFFF";
    test -= 0.35;
    if (test < 0) {
      test = 0;
    }
    
    try {
      if (test > 1) {
        test = 1;
      }
      boolean red = false;
      int depth = 0;
      if (test < 0.5) {
        red = true;
        depth = (int) ((double) (200) / 0.5 * test);
      } else {
        test = 1 - test;
        depth = (int) ((double) (200) /0.5 * test);
      }
      String hex = Integer.toHexString(depth);
      if (hex.length() == 1) {
        hex = 0 + hex;
      }
      if (red) {
        color = "#FF" + hex + hex;
      } else {
        color = "#" + hex + "FF" + hex;
      }
    } catch (Exception e) {
    }
    return color;
  }
  
  public void setWeightedDegrees(Map<String, Double> sortedWeightedDegrees) {
    this.weightedDegress = sortedWeightedDegrees;
  }
  
  public String getMainEntity() {
    return mainEntity;
  }

  public void setMainEntity(String mainEntity) {
    this.mainEntity = mainEntity;
  }

  public void setCorrectRanking(Map<String, Integer> correctRanking) {
    this.correctRanking = correctRanking;
  }

  public void addEntityContext(String entity, Map<String, Double> context) {
    entityContext.put(entity, context);
  }
  
  public Map<String, Double> getEntityContext(String entity) {
    return entityContext.get(entity);
  }
}
