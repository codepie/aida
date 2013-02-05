package mpi.aida.preparation;

import mpi.tokenizer.data.Tokenizer;
import mpi.tokenizer.data.TokenizerManager;
import mpi.tokenizer.data.Tokens;

public class AidaTokenizerManager {
  public static void init() {
    TokenizerManager.init();
  }

  public static Tokens tokenize(String docId, String text, Tokenizer.type type, boolean lemmatize) {
    Tokens tokens = TokenizerManager.parse(docId, text, type, lemmatize);
    return tokens;
  }
}
