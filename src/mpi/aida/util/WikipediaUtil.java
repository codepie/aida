package mpi.aida.util;

import java.util.regex.Pattern;

public class WikipediaUtil {

  public static final int TOTAL_DOCS = 2628265;

  /**
   * Returns ONLY text (minus headlines, links, etc.) for a Wikipedia article source
   * 
   * @param text
   * @return
   */
  public static String cleanWikipediaArticle(String text) {
    // replace newlines
    text = text.replace('\n', ' ');
    
    // remove external links
    text = text.replaceAll("(\\[https?:.+)\\[\\[[^\\[\\]]+\\]\\]", "$1");
    text = text.replaceAll("\\[https?:[^\\[\\]]+\\]", " ");

    // remove references
    text = text.replaceAll("<ref[[^<]\\n]+</ref>", "");
    text = text.replaceAll("<ref[[^<]\\n]+/>", "");
    
    // remove galleries
    text = text.replaceAll("(?s)<gallery>.*</gallery>", "");

    // remove xml tags
    text = text.replaceAll("<[^/t! ][^>]+>", " ");
    text = text.replaceAll("</[^t][^>]+>", " ");

    // remove tables
    text = Pattern.compile("<table[^>]+>(?!</table>).*</table>", Pattern.DOTALL).matcher(text).replaceAll("");

    // remove xml comments
    text = Pattern.compile("<!--.+-->", Pattern.DOTALL).matcher(text).replaceAll("");
    
    // remove all templates/macros
    text = text.replaceAll("'{2,}", "");
    text = text.replaceAll("\\[\\[[^\\[\\]]+:[^\\[\\]]+\\]\\]", "");

    // workaround for mal-formed tables
    text = Pattern.compile("\\{\\{Standard table\\|0\\}\\}.*\\{\\{close table\\}\\}", Pattern.DOTALL).matcher(text).replaceAll("");
    text = text.replaceAll("\\{\\{[sS]tart [bB]ox\\}\\}", "{|");
    text = text.replaceAll("\\{\\{[eE]nd [bB]ox\\}\\}", "|}");
    text = Pattern.compile("(?s)\\{\\|((?!\\|\\}).)*\n\\|\\}\n", Pattern.DOTALL).matcher(text).replaceAll("");

    // remove templates/infoboxes
    text = text.replaceAll("\\{\\{[[^\\{\\}]]+\\}\\}", " ");
    
    // workaround for some non-standard texts
    text = text.replaceAll("(?s)\\{\\|.*\n\\|\\}\u2020Denotes wild-card team \\(since 1995\\)\\.\n", "");
    text = Pattern.compile("^\\*{1,2}.*$", Pattern.MULTILINE).matcher(text).replaceAll("");
    text = Pattern.compile("^\\;.*$", Pattern.MULTILINE).matcher(text).replaceAll("");
    text = Pattern.compile("^:+.*$", Pattern.MULTILINE).matcher(text).replaceAll("");

    // remove [[ ... :  ... ]]
    text = text.replaceAll("\\[\\[[^\\[\\]]+:[^\\[\\]]+\\]\\]", " ");
    
    // remove headlines
    text = text.replaceAll("={2,}.*?={2,}"," ");
    
    // replace links
    text = text.replaceAll("\\[\\[[^\\]]+?\\|([^\\]\\n]+?)\\]\\]", "$1");
    text = text.replaceAll("\\[\\[([^\\]]+?)\\]\\]", "$1");

    // normalize whitespaces
    text = text.replaceAll("[\\s\\x00-\\x1F]+", " ");
    
    // normalize other characters
    text = text.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
    
    return text;
  }
}
