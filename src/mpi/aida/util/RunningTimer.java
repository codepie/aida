package mpi.aida.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Captures the running time of modules and their stages. The timing is not
 * perfectly accurate due to synchronization, however the contention should
 * be very low. Don't use this class to do high-frequency profiling, this 
 * is useful for high-level performance measuring. 
 * 
 * A module is usually a class and a stage is usually one logical
 * step inside the class.
 * 
 * @author Johannes Hoffart
 *
 */
public class RunningTimer {
  /**
   * Map is moduleId -> uniqueId -> timestamp.
   * 
   * UniqueId is an id assigned each time start(moduleId) is called. The map
   * is only accessed by start() (which is synchronized), no need to be 
   * concurrency safe. 
   * 
   */
  private static Map<String, Map<Integer, Long>> moduleStart = 
      new HashMap<String, Map<Integer, Long>>();
  private static Map<String, Map<Integer, Long>> moduleEnd = 
      new ConcurrentHashMap<String, Map<Integer, Long>>();
  
  /** 
   * Keys are moduleId:stageId.
   */
  private static Map<String, Map<Integer, Long>> moduleStageStart = 
      new ConcurrentHashMap<String, Map<Integer, Long>>();
  private static Map<String, Map<Integer, Long>> moduleStageEnd = 
      new ConcurrentHashMap<String, Map<Integer, Long>>();
     
  
  public static String getOverview() {
    StringBuilder sb = new StringBuilder();
    sb.append("Module Overview\n---\n");
    sb.append("Module\t#Docs\tAvg. Time\tMax. Time\n");
    sb.append(getDataOverview(moduleStart, moduleEnd)).append("\n");
    
    sb.append("Module Stage Overview\n---\n");
    sb.append("Module Stage\t#Docs\tAvg. Time\tMax. Time\n");
    sb.append(getDataOverview(moduleStageStart, moduleStageEnd)).append("\n");

    return sb.toString();
  }
  
  private static String getDataOverview(
      Map<String, Map<Integer, Long>> start, 
      Map<String, Map<Integer, Long>> end) {
    StringBuilder sb = new StringBuilder();
    for (String id : end.keySet()) {
      sb.append(id).append("\t");
      sb.append(end.get(id).size()).append("\t");
      
      double totalTime = 0.0;
      double maxTime = 0.0;
      for (Integer uniqueId : end.get(id).keySet()) {
        Long finish = end.get(id).get(uniqueId);
        assert start.containsKey(id) : "No start for end.";
        Long begin = start.get(id).get(uniqueId);
        Long dur = finish - begin;
        totalTime += dur;
        if (dur > maxTime) { maxTime = dur; }
      }
      double avgTime = totalTime / end.get(id).size();
      
      sb.append(NiceTime.convert(avgTime)).append("\t");
      sb.append(NiceTime.convert(maxTime)).append("\n");
    }
    return sb.toString();
  }
  
  /**
   * Starts the time for the given moduleId.
   * 
   * @param moduleId
   */
  public static synchronized Integer start(String moduleId) {
    Long timestamp = System.currentTimeMillis();
    Integer uniqueId = 0;
    Map<Integer, Long> starts = moduleStart.get(moduleId);
    if (starts != null) {
      uniqueId = moduleStart.get(moduleId).size();
    } else {
      starts = new HashMap<Integer, Long>();
      moduleStart.put(moduleId, starts);
    }
    starts.put(uniqueId, timestamp);   
    return uniqueId;
  }
  
  /**
   * Starts the timer for the given module id at the given stage. The uniqueId
   * has to correspond to what is returned by start(moduleId).
   * 
   * @param moduleId
   * @param stageId
   * @param uniqueId
   */
  public static void stageStart(String moduleId, String stageId, Integer uniqueId) {
    Long timestamp = System.currentTimeMillis();
    Map<Integer, Long> stage = moduleStageStart.get(getStageKey(moduleId, stageId));
    if (stage == null) {
      stage = new ConcurrentHashMap<Integer, Long>();
      moduleStageStart.put(getStageKey(moduleId, stageId), stage);
    }
    stage.put(uniqueId, timestamp);
  }
  
  /**
   * Takes the time for the given module id at the given stage. The uniqueId
   * has to correspond to what is returned by start(moduleId).
   * 
   * @param moduleId
   * @param stageId
   * @param uniqueId
   */
  public static void stageEnd(String moduleId, String stageId, Integer uniqueId) {
    Long timestamp = System.currentTimeMillis();
    Map<Integer, Long> stage = moduleStageEnd.get(getStageKey(moduleId, stageId));
    if (stage == null) {
      stage = new ConcurrentHashMap<Integer, Long>();
      moduleStageEnd.put(getStageKey(moduleId, stageId), stage);
    }
    stage.put(uniqueId, timestamp);
  }
  
  /**
   * Halts the timer for the given moduleId, capturing the full time of 
   * the module. The uniqueId has to correspond to what is returned by 
   * start(moduleId).
   * 
   * @param moduleId
   */
  public static void end(String moduleId, Integer uniqueId) {
    Long timestamp = System.currentTimeMillis();
    Map<Integer, Long> end = moduleEnd.get(moduleId);
    if (end == null) {
      end = new ConcurrentHashMap<Integer, Long>();
      moduleEnd.put(moduleId, end);
    }
    end.put(uniqueId, timestamp);
  }
  
  private static String getStageKey(String moduleId, String stageId) {
    return moduleId + ":" + stageId;
  }
}
