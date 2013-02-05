package mpi.aida.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class CollectionUtils {
  public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortMapByValue(Map<K, V> map) {
    return sortMapByValue(map, false);
  }
  
  public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortMapByValue(Map<K, V> map, final boolean descending) {
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {

      public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
        int comp = (o1.getValue()).compareTo(o2.getValue());
        
        if (descending) {
          comp = comp * (-1);
        }
        
        return comp;
      }
    });

    LinkedHashMap<K, V> result = new LinkedHashMap<K, V>();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }
}
