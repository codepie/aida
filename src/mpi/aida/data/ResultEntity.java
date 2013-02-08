package mpi.aida.data;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Entity the was assigned to a ResultMention.
 * The entity String is the identifier in YAGO2 
 * (see http://www.yago-knowledge.org)
 * 
 *
 */
public class ResultEntity implements Comparable<ResultEntity>, Serializable {

  private static final long serialVersionUID = -7062155406718136994L;

  /** YAGO2 identifier of the entity (http://www.yago-knowledge.org) */
  private String entity;

  /** Score assigned to the entity */
  private double disambiguationScore;

  public ResultEntity(String entity, double disambiguationScore) {
    super();
    this.entity = entity;
    this.disambiguationScore = disambiguationScore;
  }

  public static ResultEntity getNoMatchingEntity() {
    return new ResultEntity(Entity.NO_MATCHING_ENTITY, 0.0);
  }

  public static List<ResultEntity> getResultEntityAsList(ResultEntity re) {
    List<ResultEntity> res = new ArrayList<ResultEntity>(1);
    res.add(re);
    return res;
  }

  /**
   * @return  YAGO2 identifier of the entity (http://www.yago-knowledge.org)
   */
  public String getEntity() {
    return entity;
  }

  public void setEntity(String entity) {
    this.entity = entity;
  }

  public double getDisambiguationScore() {
    return disambiguationScore;
  }

  public void setDisambiguationScore(double disambiguationScore) {
    this.disambiguationScore = disambiguationScore;
  }
  
  public boolean isNoMatchingEntity() {
    return entity.equals(Entity.NO_MATCHING_ENTITY);
  }

  @Override
  public int compareTo(ResultEntity re) {
    // natural ordering for ResultEntities is descending
    return new Double(new Double(re.getDisambiguationScore())).compareTo(disambiguationScore);
  }

  public String toString() {
    NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
    df.setMaximumFractionDigits(5);
    return entity + " (" + df.format(disambiguationScore) + ")";
  }
}
