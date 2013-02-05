package mpi.aida.graph.similarity.measure;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import mpi.aida.AidaManager;
import mpi.aida.data.Context;
import mpi.aida.data.Entity;
import mpi.aida.data.Mention;
import mpi.aida.graph.similarity.context.EntitiesContext;
import mpi.experiment.trace.Tracer;

/**
 * This class calculates the similarity between a mention and an
 * entity context by a dot product between the word count vectors.
 * 
 * @author Johannes Hoffart
 *
 */
public class WordCountVectorDotProductSimilarityMeasure extends MentionEntitySimilarityMeasure {

  public WordCountVectorDotProductSimilarityMeasure(Tracer tracer) {
    super(tracer);
  }

  @Override
  public double calcSimilarity(Mention mention, Context context, Entity entity, EntitiesContext entitiesContext) {
    // create two Maps representing the word count vectors
    TIntIntHashMap contextVec = createWordCountVector(context.getTokenIds());
    TIntIntHashMap entityVec = createWordCountVector(entitiesContext.getContext(entity));

    // calc dot product between them
    double similarity = calcDotProduct(entityVec, contextVec);
    return similarity;
  }

  private TIntIntHashMap createWordCountVector(int[] is) {
    TIntIntHashMap wordCountVector = new TIntIntHashMap();

    for (int word : is) {
      wordCountVector.adjustOrPutValue(word, 1, 1);
    }

    return wordCountVector;
  }

  private double calcDotProduct(
      TIntIntHashMap entityVec, TIntIntHashMap contextVec) {
    int dotProduct = 0;

    for (TIntIntIterator it = entityVec.iterator(); it.hasNext(); ) {
      it.advance();
      int wordA = it.key();

      int expandedA = AidaManager.expandTerm(wordA);

      // get counts of word in both vectors
      int wordAcount = entityVec.get(wordA);
      int wordBcount = contextVec.get(wordA);

      wordBcount += contextVec.get(expandedA); // add expanded count if available

      int temp = wordAcount * wordBcount;
      dotProduct += temp;
    }

    return dotProduct;
  }
}
