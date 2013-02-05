package mpi.aida.preparation.mentionrecognition;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javatools.datatypes.Pair;
import mpi.aida.data.Mentions;
import mpi.aida.data.PreparedInput;
import mpi.tokenizer.data.Token;
import mpi.tokenizer.data.Tokens;

public class FilterMentions implements Serializable {

  private static final long serialVersionUID = 6260499966421708963L;

  private NamedEntityFilter namedEntityFilter = null;

  private ManualFilter manualFilter = null;

  private HybridFilter hybridFilter = null;

  public FilterMentions() {
    namedEntityFilter = new NamedEntityFilter();
    manualFilter = new ManualFilter();
    hybridFilter = new HybridFilter();
  }

  /** which type of tokens to get*/
  public static enum FilterType {
    STANFORD_NER, Manual, ManualPOS, Manual_NER, Hybrid, None;
  };

  public PreparedInput filter(String text, String docId, Tokens tokens, FilterType by) {
    Mentions mentions = null;
    Tokens returnTokens = null;
    if (by.equals(FilterType.STANFORD_NER)) {
      mentions = namedEntityFilter.filter(tokens);
      returnTokens = tokens;
    } else if (by.equals(FilterType.Manual) || by.equals(FilterType.ManualPOS) || by.equals(FilterType.Manual_NER)) {
      Pair<Tokens, Mentions> tokensMentions = manualFilter.filter(text, docId, by);
      mentions = tokensMentions.second();
      returnTokens = tokensMentions.first();
    } else if (by.equals(FilterType.Hybrid)) {
      Pair<Tokens, Mentions> tokensMentions = manualFilter.filter(text, docId, by);
      Mentions manualMentions = tokensMentions.second();
      Mentions NERmentions = namedEntityFilter.filter(tokensMentions.first());
      mentions = hybridFilter.parse(manualMentions, NERmentions);
      returnTokens = tokensMentions.first();
    } else if (by.equals(FilterType.None)) {
      mentions = new Mentions();
      List<String> tokenlist = new LinkedList<String>();
      for (int p = 0; p < tokens.size(); p++) {
        Token token = tokens.getToken(p);
        tokenlist.add(token.getOriginal());
      }
      returnTokens = tokens;
    }
    PreparedInput preparedInput = new PreparedInput(docId, returnTokens, mentions);
    return preparedInput;
  }
}