package mpi.aida.preparation.mentionrecognition;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mpi.aida.data.Mention;
import mpi.aida.data.Mentions;
import mpi.tokenizer.data.Token;
import mpi.tokenizer.data.Tokens;

public class NamedEntityFilter {

  private HashMap<String, String> tags = null;

  public NamedEntityFilter() {
    tags = new HashMap<String, String>();
    tags.put("LOCATION", "LOCATION");
    tags.put("I-LOC", "I-LOC");
    tags.put("B-LOC", "I-LOC");
    tags.put("PERSON", "PERSON");
    tags.put("I-PER", "I-PER");
    tags.put("B-PER", "I-PER");
    tags.put("ORGANIZATION", "ORGANIZATION");
    tags.put("I-ORG", "I-ORG");
    tags.put("B-ORG", "I-ORG");
    tags.put("MISC", "MISC");
    tags.put("I-MISC", "I-MISC");
    tags.put("B-MISC", "I-MISC");
  }

  public Mentions filter(Tokens tokens) {
    Mentions mentions = new Mentions();
    HashMap<Integer, Integer> subStrings = new HashMap<Integer, Integer>();
    List<String> content = new LinkedList<String>();
    for (int p = 0; p < tokens.size(); p++) {
      Token token = tokens.getToken(p);
      content.add(token.getOriginal());
    }
    String previous = null;
    int start = -1;
    int end = -1;
    for (int p = 0; p < tokens.size(); p++) {
      Token token = tokens.getToken(p);
      if (previous == null) {
        if (tags.containsKey(token.getNE())) {
          previous = tags.get(token.getNE());
          start = token.getId();
          end = token.getId();
        }
      } else if (previous.equals(token.getNE())) {
        end = token.getId();
      } else {
        Mention newMentions = getPossibleMentions(start, end, tokens);
        mentions.addMention(newMentions);
        subStrings.put(start, end);
        previous = null;
        if (tags.containsKey(token.getNE())) {
          previous = tags.get(token.getNE());
          start = token.getId();
          end = token.getId();
        }
      }
    }
    if (previous != null) {
      Mention newMentions = getPossibleMentions(start, end, tokens);
      mentions.addMention(newMentions);
      subStrings.put(start, end);
      previous = null;
    }
    mentions.setSubstring(subStrings);
    return mentions;
  }

  private Mention getPossibleMentions(int start, int end, Tokens advTokens) {
    String meansArg = advTokens.toText(start, end);
    int startStanford = advTokens.getToken(start).getStandfordId();
    int sentenceId = advTokens.getToken(start).getSentence();
    int endStanford = advTokens.getToken(end).getStandfordId();
    Mention mention = new Mention(meansArg, start, end, startStanford, endStanford, sentenceId);
    int firstChar = advTokens.getToken(mention.getStartToken()).getBeginIndex();
    int lastChar = advTokens.getToken(mention.getEndToken()).getEndIndex();
    int charLength = lastChar - firstChar;
    mention.setCharOffset(firstChar);
    mention.setCharLength(charLength);
    return mention;
  }
}
