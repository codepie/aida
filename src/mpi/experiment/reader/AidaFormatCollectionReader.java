package mpi.experiment.reader;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javatools.util.FileUtils;
import mpi.aida.data.Context;
import mpi.aida.data.Mention;
import mpi.aida.data.Mentions;
import mpi.aida.data.PreparedInput;
import mpi.tokenizer.data.Token;
import mpi.tokenizer.data.Tokens;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AidaFormatCollectionReader extends CollectionReader {
  private static final Logger logger = 
      LoggerFactory.getLogger(AidaFormatCollectionReader.class);
  
  private List<String> experimentDocIds;

  private List<String> allDocIds = null;

  private HashMap<String, String> text = null;

  private HashMap<String, Tokens> tokensMap = null;

  private HashMap<String, Mentions> mentionsMap = null;

  public AidaFormatCollectionReader(String collectionPath, String fileName) {
    this(collectionPath, fileName, 0, Integer.MAX_VALUE);
  }

  public AidaFormatCollectionReader(String collectionPath, String fileName, int from, int to) {
    super(collectionPath, from, to);
    this.filePath = collectionPath + fileName;
    allDocIds = new LinkedList<String>();
    initFile();
    initPreparedInputs();
  }

  public AidaFormatCollectionReader(String collectionPath, String fileName, CollectionPart cp) {
    super(collectionPath, cp);
    this.filePath = collectionPath + fileName;
    allDocIds = new LinkedList<String>();
    initFile();
    initPreparedInputs();
  }

  public AidaFormatCollectionReader(String collectionPath, String fileName, String docNums) {
    super(collectionPath, docNums);
    this.filePath = collectionPath + fileName;
    allDocIds = new LinkedList<String>();
    initFile();
    initPreparedInputs();
  }

  private HashSet<String> getPuncuations() {
    HashSet<String> punctuations = null;
    punctuations = new HashSet<String>();
    punctuations.add(".");
    punctuations.add(":");
    punctuations.add(",");
    punctuations.add(";");
    punctuations.add("!");
    punctuations.add("?");
    punctuations.add("'s");
    return punctuations;
  }

  private void initPreparedInputs() {
    experimentDocIds = new LinkedList<String>();
    Iterator<String> allDocIdsIterator = allDocIds.iterator();
    int counter = 0;
    if (allDocNumbers != null) {
      while (allDocIdsIterator.hasNext()) {
        String id = allDocIdsIterator.next();
        counter++;
        if (allDocNumbers.contains(counter)) {
          experimentDocIds.add(id);
        }
      }
    } else {
      while (allDocIdsIterator.hasNext()) {
        String id = allDocIdsIterator.next();
        counter++;
        if (counter >= from && counter <= to) {
          experimentDocIds.add(id);
        } else if (counter >= to) {
          break;
        }
      }
    }
    preparedInputs = new ArrayList<PreparedInput>(experimentDocIds.size());
    for (String docId : experimentDocIds) {
      preparedInputs.add(new PreparedInput(docId, tokensMap.get(docId), mentionsMap.get(docId)));
    }
  }

  private void initFile() {
    if (filePath == null) {
      logger.error("File path is null");
      return;
    }
    File f = new File(filePath);
    if (f.exists()) {
      load(f);
    } else {
      logger.error("File does not exist " + filePath);
    }
  }

  private void load(File f) {
    logger.info("Loading collection from " + f.getName());
    text = new HashMap<String, String>();
    tokensMap = new HashMap<String, Tokens>();
    mentionsMap = new HashMap<String, Mentions>();
    HashSet<String> punctuations = getPuncuations();
    int sentence = -1;
    Tokens tokens = null;
    Mentions mentions = null;
    try {
      int position = -1;
      int index = 0;
      //Reader reader = new FileReader(f);
      BufferedReader breader = FileUtils.getBufferedUTF8Reader(f);//new BufferedReader(reader);
      String line = breader.readLine();
      while (line != null) {
        if (line.startsWith("-DOCSTART-")) {
          int start = line.indexOf("(");
          String docId = line.substring(start + 1, line.length() - 1);
          if (tokens != null) {
            setTokensPositions(mentions, tokens);
            text.put(tokens.getDocId(), tokens.toText());
            tokensMap.put(tokens.getDocId(), tokens);
            mentionsMap.put(tokens.getDocId(), mentions);
            index = 0;
            position = -1;
            List<String> content = new LinkedList<String>();
            for (int p = 0; p < tokens.size(); p++) {
              Token token = tokens.getToken(p);
              content.add(token.getOriginal());
            }
          }
          tokens = new Tokens(docId);
          allDocIds.add(docId);
          mentions = new Mentions();
          sentence = 0;
        } else {
          if (line.trim().length() == 0) {
            sentence++;
            line = breader.readLine();
            continue;
          }
          position++;
          String[] data = line.split("\t");
          boolean mentionStart = false;
          String word = null;
          String textMention = null;
          String entity = null;
          String ner = null;
          if (data.length == 0) {
            System.out.println("Line length 0 for doc id " + tokens.getDocId());
          } else if (data.length == 1) {
            word = data[0];
          } else if (data.length == 4) {
            word = data[0];
            mentionStart = "B".equals(data[1]);
            textMention = data[2];
            entity = data[3];
          } else if (data.length == 5) {
            word = data[0];
            mentionStart = "B".equals(data[1]);
            textMention = data[2];
            entity = data[3];
            ner = data[4];
          } else {
            logger.warn("line has wrong format " + line + " for docId " + tokens.getDocId());
          }

          if (punctuations.contains(word) && tokens.size() > 0) {
            Token at = tokens.getToken(tokens.size() - 1);
            at.setOriginalEnd("");
            index = index - 1;
          }
          int endIndex = index + word.length();
          Token at = new Token(position, word, " ", index, endIndex, sentence, 0, null, ner);
          tokens.addToken(at);
          if (textMention != null && mentionStart) {
            Mention mention = new Mention();
            mention.setCharOffset(index);
            mention.setCharLength(textMention.length());
            mention.setMention(textMention);
            mention.setGroundTruthResult(entity);
            mentions.addMention(mention);
          }
          index = endIndex + 1;
        }
        line = breader.readLine();
      }
      if (tokens != null) {
        List<String> content = new LinkedList<String>();
        for (int p = 0; p < tokens.size(); p++) {
          Token token = tokens.getToken(p);
          content.add(token.getOriginal());
        }
        setTokensPositions(mentions, tokens);
        text.put(tokens.getDocId(), tokens.toText());
        tokensMap.put(tokens.getDocId(), tokens);
        mentionsMap.put(tokens.getDocId(), mentions);
      }
      breader.close();
    } catch (Exception e) {
      logger.error(e.getLocalizedMessage());
    }
  }

  private void setTokensPositions(Mentions mentions, Tokens tokens) {
    int startToken = -1;
    int endToken = -1;
    int t = 0;
    int i = 0;
    Mention mention = null;
    Token token = null;
    while (t < tokens.size() && i < mentions.getMentions().size()) {
      mention = mentions.getMentions().get(i);
      token = tokens.getToken(t);
      if (startToken >= 0) {
        if (token.getEndIndex() > mention.getCharOffset() + mention.getCharLength()) {
          mention.setStartToken(startToken);
          mention.setId(startToken);
          mention.setEndToken(endToken);
          if (mention.getMention() == null) {
            mention.setMention(tokens.toText(startToken, endToken));
          }
          startToken = -1;
          endToken = -1;
          i++;
        } else {
          endToken = token.getId();
          t++;
        }
      } else {
        if (token.getBeginIndex() >= mention.getCharOffset() && mention.getCharOffset() <= token.getEndIndex()) {
          startToken = token.getId();
          endToken = token.getId();
        } else {
          t++;
        }
      }
    }
    if (startToken >= 0) {
      if (token.getEndIndex() >= mention.getCharOffset() + mention.getCharLength()) {
        mention.setStartToken(startToken);
        mention.setId(startToken);
        mention.setEndToken(endToken);
      }
    }
  }

  public List<String> getAllDocIds() {
    return allDocIds;
  }

  @Override
  public int collectionSize() {
    return experimentDocIds.size();
  }

  protected abstract int[] getCollectionPartFromTo(CollectionPart cp);

  @Override
  public Iterator<PreparedInput> iterator() {
    return preparedInputs.iterator();
  }

  @Override
  public Mentions getDocumentMentions(String docId) {
    return getDocumentMentions(docId, includeNMEMentions);
  }

  public String getText(String docId) {
    return text.get(docId);
  }

  public HashMap<String, String> getTextMap() {
    return text;
  }

  public Tokens getTokensForDocIdMap(String docId) {
    return tokensMap.get(docId);
  }

  public HashMap<String, Tokens> getTokensMap() {
    return tokensMap;
  }

  public Mentions getDocumentMentions(String docId, boolean includeEmptyMentions) {
    if (includeEmptyMentions) {
      return mentionsMap.get(docId);
    } else {
      Mentions ms = mentionsMap.get(docId);
      Mentions nonEmptyMentions = new Mentions();
      for (Mention m : ms.getMentions()) {
        if (!m.getGroundTruthResult().equals("--NME--")) {
          nonEmptyMentions.addMention(m);
        }
      }
      return nonEmptyMentions;
    }
  }

  @Override
  public Context getDocumentContext(String docId) {
    return new Context(getTokensForDocIdMap(docId));
  }
}
