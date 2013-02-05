package mpi.aida.data;

import static org.junit.Assert.assertEquals;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.LinkedList;
import java.util.List;

import mpi.aida.access.DataAccess;
import mpi.aida.config.AidaConfig;

import org.junit.Test;


public class ContextTest {

  public ContextTest() {
    AidaConfig.set("dataAccess", "testing");
  }
  
  @Test
  public void test() {
    List<String> text = new LinkedList<String>();
    
    text.add("Jimmy");
    text.add("played");
    text.add("Les");
    text.add("Paul");
    text.add("played");    
    
    Context context = new Context(text);
    assertEquals(text, context.getTokens());    
    TIntObjectHashMap<String> id2word =
        DataAccess.getWordsForIds(context.getTokenIds());
    
    for (int i = 0; i < text.size(); ++i) {
      assertEquals(text.get(i), id2word.get(context.getTokenIds()[i]));
    }
  } 
}
