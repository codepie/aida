package mpi.aida.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.experiment.trace.Tracer;

public class DisambiguationResults implements Serializable {

  private static final long serialVersionUID = 8366493180300359941L;

  private Map<ResultMention, List<ResultEntity>> mentionMappings;

  private String gTracerHtml;

  private Tracer tracer = null;

  public DisambiguationResults(Map<ResultMention, List<ResultEntity>> mentionMappings, String gTracerHtml) {
    super();
    this.mentionMappings = mentionMappings;
    this.gTracerHtml = gTracerHtml;
  }

  public List<ResultMention> getResultMentions() {
    List<ResultMention> mentions = new ArrayList<ResultMention>(mentionMappings.keySet());
    Collections.sort(mentions);
    return mentions;
  }

  public List<ResultEntity> getResultEntities(ResultMention rm) {
    return mentionMappings.get(rm);
  }

  public void setResultEntities(ResultMention rm, List<ResultEntity> res) {
    mentionMappings.put(rm, res);
  }

  public ResultEntity getBestEntity(ResultMention rm) {
    List<ResultEntity> res = getResultEntities(rm);

    if (res.size() == 0) {
      return null;
    } else {
      return res.get(0);
    }
  }

  /**
   * THIS METHOD IS DEPRECATED!
   * Please use getResultMentions() and getResultEntities()/getBestEntity()
   * 
   * Return a map from all mentions found in the input document
   * to the best entity it could be disambiguated to.
   * 
   * Mentions are in the format: mention name:::character-offset:::character-length:::score
   * Entities are a String identifying the YAGO2 entity (see http://www.yago-knowledge.org)
   * 
   * @return  Map of mentions to the best entity
   */
  @Deprecated
  public Map<String, String> getMentionMappings() {
    Map<String, String> mappings = new HashMap<String, String>();

    for (ResultMention rm : getResultMentions()) {
      String entityId = null;
      ResultEntity re = getBestEntity(rm);
      if (re != null) {
        entityId = re.getEntity();
      }

      mappings.put(rm.getMention() + ":::" + rm.getCharacterOffset() + ":::" + rm.getCharacterLength() + ":::" + re.getDisambiguationScore(), entityId);
    }

    return mappings;
  }

  public String getgTracerHtml() {
    return gTracerHtml;
  }

  public void setgTracerHtml(String gTracerHtml) {
    this.gTracerHtml = gTracerHtml;
  }

  public Tracer getTracer() {
    return tracer;
  }

  public void setTracer(Tracer tracer) {
    this.tracer = tracer;
  }

  public String toString() {
    return mentionMappings.toString();
  }
}
