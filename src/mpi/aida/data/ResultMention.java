package mpi.aida.data;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mention detected in the input text. It is identified uniquely
 * by the combination of the three members docId+mention+characterOffset.
 * 
 * @author Johannes Hoffart
 *
 */
public class ResultMention implements Comparable<ResultMention>, Serializable {
  private static final Logger logger = 
      LoggerFactory.getLogger(ResultMention.class);
  
  private static final long serialVersionUID = -6791087404868641006L;

  private String docId;

  private String mention;

  private int characterOffset;

  private int characterLength;

  public ResultMention(String docId, String mention, int characterOffset, int characterLength) {
    super();
    this.docId = docId;
    this.mention = mention;
    this.characterOffset = characterOffset;
    this.characterLength = characterLength;
  }

  public String getDocId() {
    return docId;
  }

  public void setDocId(String docId) {
    this.docId = docId;
  }

  public String getMention() {
    return mention;
  }

  public void setMention(String mention) {
    this.mention = mention;
  }

  public int getCharacterOffset() {
    return characterOffset;
  }

  public void setCharacterOffset(int characterOffset) {
    this.characterOffset = characterOffset;
  }

  public int getCharacterLength() {
    return characterLength;
  }

  public void setCharacterLength(int characterLength) {
    this.characterLength = characterLength;
  }

  public static ResultMention getResultMentionFromMentionString(String docId, String mentionString) {
    String[] data = mentionString.split(":::");

    if (data.length < 3) {
      logger.error("Could not create ResultMention from mentionString: " + mentionString);
      return null;
    }

    String mention = data[0];
    int characterOffset = Integer.parseInt(data[1]);
    int characterLength = Integer.parseInt(data[2]);

    ResultMention rm = new ResultMention(docId, mention, characterOffset, characterLength);
    return rm;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ResultMention) {
      ResultMention rm = (ResultMention) o;
      return (docId.equals(rm.getDocId()) && mention.equals(rm.getMention()) && characterOffset == rm.getCharacterOffset());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return docId.hashCode() + mention.hashCode() + characterOffset;
  }

  @Override
  public int compareTo(ResultMention rm) {
    int result = docId.compareTo(rm.getDocId());

    if (result == 0) {
      result = new Integer(characterOffset).compareTo(new Integer(rm.getCharacterOffset()));
    }

    return result;
  }

  public String toString() {
    return "[" + docId + "] " + mention + " (" + characterOffset + "/" + characterLength + ")";
  }
}
