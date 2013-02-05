package mpi.experiment.trace.measures;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

import mpi.aida.util.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KeytermEntityEntityMeasureTracer extends MeasureTracer {
  private static final Logger logger = 
      LoggerFactory.getLogger(KeytermEntityEntityMeasureTracer.class);
  
  Map<String, Double> terms;
  Map<String, TermTracer> matchedTerms;
  
  private DecimalFormat sFormatter = new DecimalFormat("0.0E0");
  private DecimalFormat percentFormatter = new DecimalFormat("#0.0");

  public static final String UI_PREFIX = "KWCSEEMT";
  public static int countForUI = 0;
  
  public KeytermEntityEntityMeasureTracer(String name, double weight, Map<String, Double> terms, Map<String, TermTracer> matchedTerms) {
    super(name, weight);
    
    this.terms = terms;
    this.matchedTerms = matchedTerms;
  }

  @Override
  public String getOutput() {       
    int keywordCount=0;
    
    StringBuilder sb = new StringBuilder();
    
//    sb.append("&nbsp;&nbsp;&nbsp;&nbsp;<em>eesim: " + weight + "</em><br />");
    
    Map<String, TermTracer> sortedMatches = CollectionUtils.sortMapByValue(matchedTerms, true);
    
    double totalWeight = 0.0;
    for (TermTracer tt : matchedTerms.values()) {
      totalWeight += tt.getTermWeight();
    }
    
    double currentWeight = 0.0;
    
    for (Entry<String, TermTracer> k : sortedMatches.entrySet()) {  
      String term = k.getKey();      
      keywordCount++;
                  
      if(keywordCount == 1) {
        countForUI++;
        sb.append(" <a onclick=\"setVisibility('div"
            + UI_PREFIX + countForUI
            + "', 'block');\">More ...</a>&nbsp;&nbsp;&nbsp;&nbsp;<a onclick=\"setVisibility('div"
            + countForUI + "', 'none');\">Less ...</a>");
        sb.append("<div id='div" + UI_PREFIX + countForUI + "' style='display:none'>");
      }
      
      for (String inner : term.split(" ")) {
        if ((k.getValue().getInnerMatches() != null) && k.getValue().getInnerMatches().containsKey(inner)) {
          sb.append("<span style='background-color:#FFAA70;'>").append(inner).append(" (").append(sFormatter.format(k.getValue().getInnerMatches().get(inner))).append(")</span> ");
        } else {
          sb.append(inner).append(" ");
        }
      }
      sb.append(": ").append(sFormatter.format(terms.get(term)));
//      part = "<span style='background-color: #ADFF2F;'>" +
      try {
        if (terms.containsKey(term)) {
          double matchWeight = matchedTerms.get(term).getTermWeight();
          currentWeight += matchWeight;      
          sb.append(" (contrib. " + sFormatter.format(matchWeight) + ")");
        }
      } catch (IllegalArgumentException e) {
        logger.warn("Could not format weight for '" + 
                    term + "': " + terms.get(term));
      }
            
      sb.append("<br />");
      
      if (keywordCount % 10 == 0) {
        double percent = currentWeight / totalWeight * 100;
        sb.append("<div style='margin: 5px auto 5px 10px; font-weight:bold;'>" + sFormatter.format(currentWeight) + " (" + percentFormatter.format(percent) + "%)</div>");
      }
    }
    
    if (keywordCount >= 1) {
      sb.append("</div>");
    }

    sb.append("<div style='font-weight:bold;text-align:right;width:80%'>Matches: " + keywordCount + "/" + terms.size() + "</div>");
    
    return sb.toString();
  }

  public Map<String, TermTracer> getMatchedKeywords() {
    return matchedTerms;
  }
}
