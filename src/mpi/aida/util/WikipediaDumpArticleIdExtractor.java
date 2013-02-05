package mpi.aida.util;

import java.io.Reader;

import javatools.filehandlers.FileLines;
import javatools.parsers.Char;
import javatools.util.FileUtils;

/**
 * Extracts all article ids from a Wikipedia pages-articles dump.
 * Output format is:
 * article_title<TAB>id
 * 
 * @author Johannes Hoffart
 *
 */
public class WikipediaDumpArticleIdExtractor {

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      printUsage();
      System.exit(1);
    }
    
    final Reader reader = FileUtils.getBufferedUTF8Reader(args[0]);
    String page = FileLines.readBetween(reader, "<page>", "</page>");
    
    int pagesDone = 0;
    
    while (page != null) {
      if (++pagesDone % 100000 == 0) {
        System.err.println(pagesDone + " pages done.");
      }
      
      page = Char.decodeAmpersand(page.replace("&amp;", "&"));
      String title = FileLines.readBetween(page, "<title>", "</title>");
      String id = FileLines.readBetween(page, "<id>", "</id>");
      String wpUrl = "http://en.wikipedia.org/wiki/" + title.replace(' ', '_');
      System.out.println(wpUrl + "\t" + id);
      
      page = FileLines.readBetween(reader, "<page>", "</page>");
    }
  }

  public static void printUsage() {
    System.out.println("Usage:");
    System.out.println("\tWikipediaDumpArticleIdExtractor <wikipedia-pages-articles.xml>");
  }
}