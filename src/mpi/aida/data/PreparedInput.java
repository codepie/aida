package mpi.aida.data;

import mpi.tokenizer.data.Tokens;

public class PreparedInput {

  private String docId;

  private Tokens tokens;

  /** Used by the local similarity methods in the disambiguation. It holds
   * the document tokens both as strings and converted to word ids. */ 
  private Context context;
  
  private Mentions mentions;

  public PreparedInput(String docId) {
    this.docId = docId;
  }

  public PreparedInput(String docId, Tokens tokens, Mentions mentions) {
    this.docId = docId;
    this.tokens = tokens;
    this.mentions = mentions;
    context = createContextFromTokens(tokens);
  }

  public Tokens getTokens() {
    return tokens;
  }

  public void setTokens(Tokens tokens) {
    this.tokens = tokens;
    context = createContextFromTokens(tokens);
  }

  public Mentions getMentions() {
    return mentions;
  }

  public void setMentions(Mentions mentions) {
    this.mentions = mentions;
  }
  
  public Context getContext() {
    return context;
  }

  private Context createContextFromTokens(Tokens t) {
    return new Context(t);
  }

  public String getDocId() {
    return docId;
  }
}
