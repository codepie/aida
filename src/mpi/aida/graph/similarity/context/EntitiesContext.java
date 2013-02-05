package mpi.aida.graph.similarity.context;

import java.util.LinkedList;
import java.util.List;

import mpi.aida.AidaManager;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.tokenizer.data.Token;
import mpi.tokenizer.data.Tokens;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import basics.Normalize;

public abstract class EntitiesContext {
  private static final Logger logger = 
      LoggerFactory.getLogger(EntitiesContext.class);
  
  protected Entities entities;
  protected EntitiesContextSettings settings;

  public EntitiesContext(Entities entities, EntitiesContextSettings settings) throws Exception {
    this.entities = entities;
    this.settings = settings;

    long beginTime = System.currentTimeMillis();

    setupEntities(entities);

    long runTime = (System.currentTimeMillis() - beginTime) / 1000;
    logger.debug("Done setting up " + this + ": " + runTime + "s");
  }

  public void setEntities(Entities entities) throws Exception {
    this.entities = entities;
    setupEntities(entities);
  }

  public Entities getEntities() {
    return entities;
  }

  public abstract int[] getContext(Entity entity);

  protected abstract void setupEntities(Entities entities) throws Exception;

  protected List<String> getTokens(String string) {
    List<String> tokens = new LinkedList<String>();

    Tokens advTokens = AidaManager.tokenize("EntitiesContext", string);

    for (Token token : advTokens) {
      tokens.add(token.getOriginal());
    }

    return tokens;
  }

  public static String getEntityName(String entity) {
    String norm = Normalize.unEntity(entity);
    norm = norm.replaceAll(" \\(.*?\\)$", "");

    return norm;
  }
  
  public String toString() {
    return getIdentifier();
  }
  
  public String getIdentifier() {
    return this.getClass().getSimpleName();
  }
}
