package mpi.aida.util;

import java.util.Comparator;
import java.util.HashMap;

public class SortByAvgPre implements Comparator<String> {

  private HashMap<String, String> idsAvgPrec = null;

  public SortByAvgPre(HashMap<String, String> idsAvgPrec) {
    this.idsAvgPrec = idsAvgPrec;
  }

  @Override
  public int compare(String o1, String o2) {
    if (idsAvgPrec.get(o1) == null && idsAvgPrec.get(o2) == null) {
      return 0;
    } else if (idsAvgPrec.get(o1) == null || idsAvgPrec.get(o1).equals("none")) {
      return 1;
    } else if (idsAvgPrec.get(o2) == null || idsAvgPrec.get(o2).equals("none")) {
      return -1;
    }
    double first = Double.parseDouble(idsAvgPrec.get(o1));
    double second = Double.parseDouble(idsAvgPrec.get(o2));
    if (first > second) {
      return -1;
    } else if (first < second) {
      return 1;
    }
    return 0;
  }

}
