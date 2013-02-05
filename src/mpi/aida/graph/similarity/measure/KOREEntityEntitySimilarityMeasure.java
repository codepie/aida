package mpi.aida.graph.similarity.measure;

import gnu.trove.TIntCollection;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.HashMap;
import java.util.Map;

import mpi.aida.data.Entity;
import mpi.aida.graph.similarity.context.EntitiesContext;
import mpi.aida.graph.similarity.context.FastWeightedKeyphrasesContext;
import mpi.aida.util.CollectionUtils;
import mpi.experiment.trace.Tracer;
import mpi.experiment.trace.measures.KeytermEntityEntityMeasureTracer;
import mpi.experiment.trace.measures.TermTracer;

public class KOREEntityEntitySimilarityMeasure
		extends EntityEntitySimilarityMeasure {

	public KOREEntityEntitySimilarityMeasure(
			Tracer tracer) {
		super(tracer);
	}

	@Override
  public double calcSimilarity(Entity a, Entity b, EntitiesContext context) {
    FastWeightedKeyphrasesContext kwc = (FastWeightedKeyphrasesContext) context;

    // generate keyphrase pairs that intersect
    TIntObjectHashMap<TIntHashSet> overlapping = new TIntObjectHashMap<TIntHashSet>();
    for (int t : intersect(kwc.getKeywordArray(a),
        kwc.getKeywordArray(b))) {
      for (int kpA : kwc.getKeyphrasesForKeyword(a, t)) {
        for (int kpB : kwc.getKeyphrasesForKeyword(b, t)) {
          if (!overlapping.contains(kpA)) {
            overlapping.put(kpA, new TIntHashSet());
          }
          overlapping.get(kpA).add(kpB);
        }
      }
    }

    double n = .0;
    
    // tracing
//    Map<String, TermTracer> matches = new HashMap<String, TermTracer>();
    
    // iterate over overlapping phrase pairs
    for (int kpA : overlapping.keys()) {
      for (int kpB : overlapping.get(kpA).toArray()) {
        double psimn = .0; //, psimd = .0;
        for (int t : intersect(kwc.getKeyphraseTokenIds(kpA, true),
            kwc.getKeyphraseTokenIds(kpB, true))) {
          psimn += Math.min(
              kwc.getCombinedKeywordMiIdfWeight(a, t),
              kwc.getCombinedKeywordMiIdfWeight(b, t));
        }
        double kpWeight = Math.min(kwc.getCombinedKeyphraseMiIdfWeight(a, kpA), kwc.getCombinedKeyphraseMiIdfWeight(b, kpB));
        double kpJaccardSim = (psimn / (kwc.getKeywordWeightSum(a, kpA) + kwc.getKeywordWeightSum(b, kpB) - psimn));        
        double matchWeight = kpWeight * Math.pow(kpJaccardSim, 2);
        
        n += matchWeight;
        
//      TermTracer tt = new TermTracer();
//      tt.setTermWeight(matchWeight);
//      matches.put(kwc.getKeyphraseForId(kpB), tt);
      }
    }
    
    double denom = 0.0;
    
    double[] kpwA = kwc.getKeyphraseWeights(a);
    double[] kpwB = kwc.getKeyphraseWeights(b);
    for (double wa : kpwA) {
      denom += wa;
    }
    
    for (double wb : kpwB) {
      denom += wb;
    }
    
//  collectTracingInfo(aname, bname, kwc.getEntityKeyphraseIds(aname), kwc.getEntityKeyphraseIds(bname), n / deno, matches, kwc);
    
    return n / denom;
  }

	protected double getOuterDenominator(double[] kpwA, double[] kpwB, double n) {
      return zipmax(kpwA, kpwB);
  }

  @SuppressWarnings("unused")
	private void printArray(int[] arr) {
		if (arr.length > 0) {
			System.out.print("[" + arr[0]);
			for (int i = 1; i < arr.length; ++i) {
				System.out.print(", " + arr[i]);
			}
			System.out.println("]");
		} else {
			System.out.println("[]");
		}
	}
	
	@SuppressWarnings("unused")
	private double sum(double[] a) {
		double s = .0;
		for (double d : a) {
			s += d;
		}
		return s;
	}

	protected int[] intersect(int[] a, int[] b) {
		TIntCollection is = new TIntLinkedList();
		int i = 0, j = 0;
		while (i < a.length && j < b.length) {
			if (a[i] == b[j]) {
				is.add(a[i]);
				++i;
				++j;
			} else if (a[i] < b[j]) {
				++i;
			} else {
				++j;
			}
		}
		return is.toArray();
	}

	@SuppressWarnings("unused")
	private int[] union(int[] a, int[] b) {
		TIntCollection u = new TIntLinkedList();
		int i = 0, j = 0, item = -1;
		while (i < a.length && j < b.length) {
			if (a[i] == b[j]) {
				item = a[i];
				while (i < a.length && a[i] == item) {
					++i;
				}
				while (j < b.length && b[j] == item) {
					++j;
				}
			} else if (a[i] < b[j]) {
				item = a[i++];
			} else {
				item = b[j++];
			}
			u.add(item);
		}
		while (i < a.length) {
			u.add(a[i++]);
		}
		while (j < b.length) {
			u.add(b[j++]);
		}
		return u.toArray();
	}

	private double zipmax(double[] a, double[] b) {
		double s = .0;
		int i = a.length - 1, j = b.length - 1;
		while (i >= 0 && j >= 0) {
			if (a[i] >= b[j]) {
				s += a[i--] * (j + 1);
			} else {
				s += b[j--] * (i + 1);
			}
		}
		return s;
	}

	@SuppressWarnings("unused")
	private double zipmin(double[] a, double[] b) {
		double s = .0;
		int i = 0, j = 0;
		while (i < a.length && j < b.length) {
			if (a[i] <= b[j]) {
				s += a[i++] * (b.length - j);
			} else {
				s += b[j++] * (a.length - i);
			}
		}
		return s;
	}
	
  @SuppressWarnings("unused")
  private void collectTracingInfo(Entity a, Entity b, int[] kpsA, int[] kpsB, double sim, Map<String, TermTracer> matches, FastWeightedKeyphrasesContext kwc) {
    Map<String, Double> e1keyphrases = new HashMap<String, Double>();     
    for (int kp : kpsA) {
      if (kwc.getCombinedKeyphraseMiIdfWeight(a, kp) > 0.0) {
        e1keyphrases.put(kwc.getKeyphraseForId(kp), kwc.getCombinedKeyphraseMiIdfWeight(a, kp));
      }
    }     
    e1keyphrases = CollectionUtils.sortMapByValue(e1keyphrases, true);    

    Map<String, Double> e2keyphrases = new HashMap<String, Double>();
    for (int kp : kpsB) {
      if (kwc.getCombinedKeyphraseMiIdfWeight(b, kp) > 0.0) {
        e2keyphrases.put(kwc.getKeyphraseForId(kp), kwc.getCombinedKeyphraseMiIdfWeight(b, kp));
      }
    }  
    e2keyphrases = CollectionUtils.sortMapByValue(e2keyphrases, true);

    tracer.eeTracing().addEntityContext(a.getName(), e1keyphrases);
    tracer.eeTracing().addEntityContext(b.getName(), e2keyphrases);

    KeytermEntityEntityMeasureTracer mt = new KeytermEntityEntityMeasureTracer("PartialKeyphraseSim", 0.0, e2keyphrases, matches);
    mt.setScore(sim);
    tracer.eeTracing().addEntityEntityMeasureTracer(a.getName(), b.getName(), mt);

    KeytermEntityEntityMeasureTracer mt2 = new KeytermEntityEntityMeasureTracer("PartialKeyphraseSim", 0.0, e1keyphrases, matches);
    mt2.setScore(sim);
    tracer.eeTracing().addEntityEntityMeasureTracer(b.getName(), a.getName(), mt2);
  }
}