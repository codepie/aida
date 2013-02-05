package mpi.aida;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import mpi.aida.config.settings.DisambiguationSettings;
import mpi.aida.config.settings.PreparationSettings;
import mpi.aida.config.settings.disambiguation.CocktailPartyDisambiguationSettings;
import mpi.aida.config.settings.disambiguation.CocktailPartyKOREDisambiguationSettings;
import mpi.aida.config.settings.disambiguation.LocalDisambiguationSettings;
import mpi.aida.config.settings.disambiguation.PriorOnlyDisambiguationSettings;
import mpi.aida.config.settings.preparation.StanfordHybridPreparationSettings;
import mpi.aida.data.DisambiguationResults;
import mpi.aida.data.Entity;
import mpi.aida.data.PreparedInput;
import mpi.aida.data.ResultMention;
import mpi.aida.util.htmloutput.GenerateWebHtml;

/**
 * Disambiguates a document using the default PRIOR, LOCAL or GRAPH settings,
 * callable from the Command Line.
 *
 */
public class CommandLineDisambiguator {

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      printUsage();
      System.exit(1);
    }

    String disambiguationTechniqueSetting = args[0];  
    String inputFile = args[1];
    File text = new File(inputFile);

    BufferedReader reader = 
        new BufferedReader(
            new InputStreamReader(new FileInputStream(text), "UTF-8"));
    StringBuilder content = new StringBuilder();

    for (String line = reader.readLine(); line != null; 
        line = reader.readLine()) {
      content.append(line).append('\n');
    }
    reader.close();

    PreparationSettings prepSettings = new StanfordHybridPreparationSettings();
    Preparator p = new Preparator();
    PreparedInput input = 
        p.prepare(inputFile, content.toString(), prepSettings);

    DisambiguationSettings disSettings = null;   
    if (disambiguationTechniqueSetting.equals("PRIOR")) {
      disSettings = new PriorOnlyDisambiguationSettings();
    } else if (disambiguationTechniqueSetting.equals("LOCAL")) {
      disSettings = new LocalDisambiguationSettings();
    } else if (disambiguationTechniqueSetting.equals("GRAPH")) {
      disSettings = new CocktailPartyDisambiguationSettings();
    } else if (disambiguationTechniqueSetting.equals("GRAPH-KORE")) {
      disSettings = new CocktailPartyKOREDisambiguationSettings();
    } else {
      System.err.println(
          "disambiguation-technique can be either: " +
          "'PRIOR', 'LOCAL', 'GRAPH', or 'GRAPH-KORE");
      System.exit(2);
    }    
    
    Disambiguator d = new Disambiguator(input, disSettings);
    DisambiguationResults results = d.disambiguate();
    GenerateWebHtml gen = new GenerateWebHtml();
    String html = gen.process(content.toString(), input, results, false);
    
    StringBuilder sb = new StringBuilder();
    sb.append("<html><head><title>").append(inputFile).append("</title>");
    sb.append("<meta http-equiv='content-type'");
    sb.append("CONTENT='text/html; charset=utf-8' />");
    sb.append("<style type='text/css'>");
    sb.append(".eq { background-color:#87CEEB } ");
    sb.append("</style>").append("<body>");
    sb.append("<h1>").append(inputFile).append("</h1>");
    sb.append("<h2>Annotated Text</h2>");
    sb.append(html);
    sb.append("<h2>All Mappings</h2>");
    sb.append("<ul>");
    for (ResultMention rm : results.getResultMentions()) {
     sb.append("<li>" + rm + " -> " + results.getResultEntities(rm) + "</li>");
    }
    sb.append("</ul></body></html>");
    
    String resultFile = inputFile+".html";
    BufferedWriter writer = 
        new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(resultFile), "UTF-8"));
    String htmlContent = sb.toString().replaceAll("\n", "<br />");
    writer.write(htmlContent);
    writer.flush();
    writer.close();
    
    System.out.println("Disambiguation for '" + inputFile + "' done, " +
    		"result written to '" + resultFile + '"');
    
    System.out.println("Mentions and Entities found:");
    System.out.println("\tMention\tEntity\tWikipedia URL");
    for (ResultMention rm : results.getResultMentions()) {
      Entity entity = 
          AidaManager.getEntity(results.getBestEntity(rm).getEntity());
      System.out.println(
          "\t" + rm + "\t" + entity + "\t" + AidaManager.getWikipediaUrl(entity));
    }
  }

  private static void printUsage() {
    System.out.println("Usage:\n CommandLineDisambiguator " +
    		"<disambiguation-technique> <input-file.txt>");
    System.out.println("\tdisambiguation-technique: " +
    		"PRIOR, LOCAL, GRAPH, or GRAPH-KORE");
  }
}
