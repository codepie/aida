package mpi.experiment.trace.measures;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

import mpi.aida.util.CollectionUtils;


public class KeywordContextEntityTracer extends TracerPart {
   
  private Map<String, Double> keywords;
  
  private DecimalFormat formatter = new DecimalFormat("#0.00000");

  private static int countForUI;

  public static final String UI_PREFIX = "KCET";

  public KeywordContextEntityTracer(Map<String, Double> keywords) {    
    this.keywords = CollectionUtils.sortMapByValue(keywords, true);
  }

  @Override
  public String getOutput() {
    StringBuilder sb = new StringBuilder();
    
    int keywordCount = 0;
        
    for (Entry<String, Double> e : keywords.entrySet()) {
      sb.append(e.getKey()).append(" (").append(formatter.format(e.getValue())).append("), ");
      
      keywordCount++;
      
      if(keywordCount == 5) {
        countForUI++;
        sb.append(" <a onclick=\"setVisibility('div"
            + UI_PREFIX + countForUI
            + "', 'block');\">More ...</a>&nbsp;&nbsp;&nbsp;&nbsp;<a onclick=\"setVisibility('div"
            + countForUI + "', 'none');\">Less ...</a>");
        sb.append("<div id='div" + UI_PREFIX + countForUI + "' style='display:none'>");
      }      
    }
    
    if (keywordCount >= 5) {
      sb.append("</div>");
    }
    
    return sb.toString();
  }
}
