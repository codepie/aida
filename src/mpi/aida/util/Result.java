package mpi.aida.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mpi.aida.util.htmloutput.ResultMention;
import mpi.tokenizer.data.Tokens;

public class Result {

  private String text;

  private String docId;

  private List<String> dataSetIds;

  private String groundTruthId = null;

  private Tokens tokens;

  private HashMap<Integer, HashMap<String, ResultMention>> finalEntities = null;

  public Result(String docId, String text, Tokens tokens, String groundTruthId) {
    this.docId = docId;
    this.text = text;
    this.dataSetIds = new LinkedList<String>();
    this.tokens = tokens;
    finalEntities = new HashMap<Integer, HashMap<String, ResultMention>>();
    this.groundTruthId = groundTruthId;
  }

  public void addFinalentity(ResultMention entity) {
    registerDataSet(entity.getDataSetId());
    HashMap<String, ResultMention> entry = null;
    if (finalEntities.containsKey(entity.getOffset())) {
      entry = finalEntities.get(entity.getOffset());
    } else {
      entry = new HashMap<String, ResultMention>();
      finalEntities.put(entity.getOffset(), entry);
    }
    if (!entry.containsKey(entity.getOffset())) {
      entry.put(entity.getDataSetId(), entity);
    }
  }

  private void registerDataSet(String dataSetId) {
    if (!dataSetIds.contains(dataSetId)) {
      if (dataSetId.equals(groundTruthId)) {
        dataSetIds.add(0, dataSetId);
      } else {
        dataSetIds.add(dataSetId);
      }
    }
  }

  public String getDocId() {
    return docId;
  }

  public String getText() {
    return text;
  }

  public boolean containsMention(int offset) {
    return finalEntities.containsKey(offset);
  }

  public boolean containsMention(int offset, String id) {
    if (!finalEntities.containsKey(offset)) {
      return false;
    }
    return finalEntities.get(offset).containsKey(id);
  }

  public HashMap<String, ResultMention> getMention(int offset) {
    return finalEntities.get(offset);
  }

  public int size() {
    return finalEntities.size();
  }

  public Tokens getTokens() {
    return tokens;
  }

  public List<String> getDataSetIds() {
    return dataSetIds;
  }

  public void sortDataSetIds(HashMap<String, String> idsAvgPrec){
    Collections.sort(dataSetIds, new SortByAvgPre(idsAvgPrec));
    dataSetIds.remove(groundTruthId);
    dataSetIds.add(0,groundTruthId);
  }
  
  public String getGroundTruthId() {
    return groundTruthId;
  }

}
