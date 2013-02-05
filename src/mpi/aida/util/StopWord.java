package mpi.aida.util;

import gnu.trove.set.hash.TIntHashSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javatools.util.FileUtils;
import mpi.aida.access.DataAccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopWord {
  private static final Logger logger = 
      LoggerFactory.getLogger(StopWord.class);

  public static String pathStopWords = "./settings/tokens/stopwords6.txt";

  public static String pathSymbols = "./settings/tokens/symbols.txt";

  private static StopWord stopwords = null;

  private static StopWord getInstance() {
    if (stopwords == null) {
      stopwords = new StopWord();
    }
    return stopwords;
  }

  private HashSet<String> words = null;
  private TIntHashSet wordIds = null;

  private HashSet<Character> symbols = null;

  private StopWord() {
    words = new HashSet<String>();
    symbols = new HashSet<Character>();
    load();
  }

  private void load() {
    File f = new File(pathStopWords);
    if (f.exists()) {
      try {
        BufferedReader reader = FileUtils.getBufferedUTF8Reader(f);
        String word = reader.readLine().trim();
        while (word != null) {
          words.add(word.trim());
          word = reader.readLine();
        }
        reader.close();
      } catch (Exception e) {
        logger.error(e.getLocalizedMessage());
      }
    } else {
      logger.error("Path does not exists " + pathStopWords);
    }
    f = new File(pathSymbols);
    if (f.exists()) {
      try {
        BufferedReader reader = FileUtils.getBufferedUTF8Reader(f);
        String word = reader.readLine().trim();
        while (word != null) {
          words.add(word.trim());
          symbols.add(word.charAt(0));
          word = reader.readLine();
        }
        reader.close();
      } catch (Exception e) {
        logger.error(e.getLocalizedMessage());
      }
    } else {
      logger.error("Path does not exists " + pathSymbols);
    }
    wordIds = new TIntHashSet(DataAccess.getIdsForWords(words).values());
  }

  private boolean isStopWord(String word) {
    return words.contains(word);
  }
  
  private boolean isStopWord(int wordId) {
    return wordIds.contains(wordId);
  }

  private boolean isSymbol(char word) {
    return symbols.contains(word);
  }

  public static boolean is(String word) {
    return StopWord.getInstance().isStopWord(word.toLowerCase());
  }

  public static boolean is(int word) {
    return StopWord.getInstance().isStopWord(word);
  }

  public static boolean symbol(char word) {
    return StopWord.getInstance().isSymbol(word);
  }

  public static boolean firstCharSymbol(String word) {
    if (word == null || word.length() == 0) {
      return false;
    }
    return StopWord.getInstance().isSymbol(word.charAt(0));
  }

  public static void main2(String[] args) {
    HashSet<String> words = new HashSet<String>();
    File f = new File("./conf/stopwords6.txt");
    if (f.exists()) {
      try {
        FileReader fileReader = new FileReader(f);
        BufferedReader reader = new BufferedReader(fileReader);
        String word = reader.readLine();
        while (word != null) {
          if (!word.trim().equals("")) {
            words.add(word);
          }
          word = reader.readLine();
        }
        reader.close();
        fileReader.close();
      } catch (Exception e) {
        logger.error(e.getLocalizedMessage());
      }
    } else {
      logger.error("Path does not exists " + "./conf/stopwords2.txt");
    }
    f = new File("./conf/stopwords.txt");
    if (f.exists()) {
      try {
        FileReader fileReader = new FileReader(f);
        BufferedReader reader = new BufferedReader(fileReader);
        String word = reader.readLine();
        while (word != null) {
          if (!word.trim().equals("")) {
            words.add(word);
          }
          word = reader.readLine();
        }
        reader.close();
        fileReader.close();
      } catch (Exception e) {
        logger.error(e.getLocalizedMessage());
      }
    } else {
      logger.error("Path does not exists " + "./conf/stopwords.txt");
    }
    List<String> sortedWords = new LinkedList<String>();
    Iterator<String> iter = words.iterator();
    while (iter.hasNext()) {
      sortedWords.add(iter.next());
    }
    System.out.println(words.size());
    f = new File("./conf/stopwords7.txt");
    Collections.sort(sortedWords);
    try {
      FileWriter writer = new FileWriter(f);
      iter = sortedWords.iterator();
      if (iter.hasNext()) {
        writer.write(iter.next());
      }
      while (iter.hasNext()) {
        writer.write("\n" + iter.next());
      }
      writer.flush();
      writer.close();
    } catch (Exception e) {
      logger.error(e.getLocalizedMessage());
    }
    System.out.println("done");
  }

  public static void main(String[] args) {
    String test = "!";
    System.out.println(StopWord.symbol(test.charAt(0)));
  }

  public static boolean isOnlySymbols(String value) {
    for (int i = 0; i < value.length(); i++) {
      if (!StopWord.symbol(value.charAt(i))) {
        return false;
      }
    }
    return true;
  }
}
