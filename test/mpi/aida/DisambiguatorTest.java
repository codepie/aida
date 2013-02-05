package mpi.aida;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import mpi.aida.config.AidaConfig;
import mpi.aida.config.settings.DisambiguationSettings;
import mpi.aida.config.settings.PreparationSettings;
import mpi.aida.config.settings.disambiguation.CocktailPartyDisambiguationSettings;
import mpi.aida.data.DisambiguationResults;
import mpi.aida.data.PreparedInput;
import mpi.aida.data.ResultMention;
import mpi.aida.preparation.mentionrecognition.FilterMentions.FilterType;

import org.junit.Test;

/**
 * Testing against the predefined DataAccessForTesting.
 * 
 * @author Johannes Hoffart
 */
public class DisambiguatorTest {
  public static final double DEFAULT_ALPHA = 0.6;
  public static final double DEFAULT_COH_ROBUSTNESS = 0.9;
  public static final int DEFAULT_SIZE = 5;
  
  public DisambiguatorTest() {
    AidaConfig.set("dataAccess", "testing");
  }
  
  @Test
  public void testPageKashmir() throws Exception {
    Preparator p = new Preparator();

    String docId = "testPageKashmir";
    String content = "When [[Page]] played Kashmir at Knebworth, his Les Paul was uniquely tuned.";
    PreparationSettings prepSettings = new PreparationSettings();
    prepSettings.setMentionsFilter(FilterType.Hybrid);

    PreparedInput preparedInput = p.prepare(docId, content, new PreparationSettings());

    DisambiguationSettings settings = new CocktailPartyDisambiguationSettings();
    settings.setAlpha(DEFAULT_ALPHA);
    settings.setCohRobustnessThreshold(DEFAULT_COH_ROBUSTNESS);
    settings.setEntitiesPerMentionConstraint(DEFAULT_SIZE);

    Disambiguator d = new Disambiguator(preparedInput, settings);

    DisambiguationResults results = d.disambiguate();

    Map<String, String> mappings = repackageMappings(results);

    String mapped = mappings.get("Page");
    assertEquals("Jimmy_Page", mapped);

    mapped = mappings.get("Kashmir");
    assertEquals("Kashmir_(song)", mapped);

    mapped = mappings.get("Knebworth");
    assertEquals("Knebworth_Festival", mapped);

    mapped = mappings.get("Les Paul");
    assertEquals("--NME--", mapped);
  }

  private Map<String, String> repackageMappings(DisambiguationResults results) {
    Map<String, String> repack = new HashMap<String, String>();

    for (ResultMention rm : results.getResultMentions()) {
      repack.put(rm.getMention(), results.getBestEntity(rm).getEntity());
    }

    return repack;
  }
}
