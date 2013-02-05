package mpi.aida.graph.algorithms;

import java.util.List;
import java.util.Map;

import mpi.aida.data.ResultEntity;
import mpi.aida.data.ResultMention;


public abstract class DisambiguationAlgorithm {

  public abstract Map<ResultMention, List<ResultEntity>> disambiguate() throws Exception;
  
}
