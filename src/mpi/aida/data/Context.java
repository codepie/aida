package mpi.aida.data;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;

import mpi.aida.access.DataAccess;
import mpi.tokenizer.data.Token;
import mpi.tokenizer.data.Tokens;

/**
 * Holds the input document as context representation.
 * 
 * @author Johannes Hoffart
 *
 */
public class Context {

  private List<String> tokenStrings;
  private int[] tokenIds;
  
  public Context(Tokens tokens) {
    List<String> ts = new ArrayList<String>(tokens.size());
    for (Token token : tokens) {
      ts.add(token.getOriginal());
    }
    init(ts);
  }
  
  public Context(List<String> tokens) {
    init(tokens);
  }

  public void init(List<String> tokens) {
    tokenStrings = new ArrayList<String>(tokens);
    TObjectIntHashMap<String> token2ids = 
        DataAccess.getIdsForWords(tokenStrings);
    tokenIds = new int[tokens.size()];
    for (int i = 0; i < tokens.size(); ++i) {
      tokenIds[i] = token2ids.get(tokenStrings.get(i));
    }
  }
  
  public List<String> getTokens() {
    return tokenStrings;
  }
  
  public int[] getTokenIds() {
    return tokenIds;
  }
}
