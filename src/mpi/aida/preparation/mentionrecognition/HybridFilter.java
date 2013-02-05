package mpi.aida.preparation.mentionrecognition;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import mpi.aida.data.Mention;
import mpi.aida.data.Mentions;

public class HybridFilter {

  public Mentions parse(Mentions manual, Mentions ner) {
    int from = 0;
    List<Mention> toAdd = new LinkedList<Mention>();
    for (int i = 0; i < ner.getMentions().size(); i++) {
      Mention nerMention = ner.getMentions().get(i);
      boolean ok = true;
      int nerStart = nerMention.getStartToken();
      int nerEnd = nerMention.getEndToken();
      for (int m = from; m < manual.getMentions().size(); m++) {
        Mention manMention = manual.getMentions().get(m);
        int manStart = manMention.getStartToken();
        int manEnd = manMention.getEndToken();
        if (nerEnd >= manStart && nerEnd <= manEnd) {
          ok = false;
        } else if (nerStart >= manStart && nerStart <= manEnd) {
          ok = false;
        } else if (nerStart <= manStart && nerEnd >= manEnd) {
          ok = false;
        }
      }
      if (ok) {
        toAdd.add(nerMention);
      }
    }
    for (int i = 0; i < toAdd.size(); i++) {
      manual.addMention(toAdd.get(i));
    }
    Collections.sort(manual.getMentions());
    return manual;
  }
}
