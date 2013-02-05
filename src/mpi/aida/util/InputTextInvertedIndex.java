package mpi.aida.util;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.LinkedList;
import java.util.List;

import mpi.aida.data.Mention;

public class InputTextInvertedIndex {
	private TIntObjectHashMap<TIntLinkedList> indexIncludingStopWords;
	private TIntObjectHashMap<TIntLinkedList> indexWithoutStopWords;
	
	public InputTextInvertedIndex() {
		indexIncludingStopWords = new TIntObjectHashMap<TIntLinkedList>();
		indexWithoutStopWords = new TIntObjectHashMap<TIntLinkedList>(); 		
	}
	
	public InputTextInvertedIndex(int[] tokens, boolean isRemoveStopWords) {
		indexIncludingStopWords = new TIntObjectHashMap<TIntLinkedList>();
		indexWithoutStopWords = new TIntObjectHashMap<TIntLinkedList>(); 
		int noStopwordsPosition = 0;
		for (int position = 0; position < tokens.length; ++position) {
			int token = tokens[position];
			TIntLinkedList positions = indexIncludingStopWords.get(token); 
			if (positions == null) {
				positions = new TIntLinkedList();
				indexIncludingStopWords.put(token, positions);
			}
			positions.add(position);
			
			if(!isRemoveStopWords || !StopWord.is(token)) {
  			positions = indexWithoutStopWords.get(token); 
  			if (positions == null) {
  				positions = new TIntLinkedList();
  				indexWithoutStopWords.put(token, positions);
  			}
  			positions.add(noStopwordsPosition);
        noStopwordsPosition++;
			}
		}
	}
	
	public boolean containsWord(int word, Mention mention) {
		if(!indexWithoutStopWords.containsKey(word))
			return false;
		TIntLinkedList positions = indexIncludingStopWords.get(word);
		int mentionStart = mention.getStartToken();
		int mentionEnd = mention.getEndToken();
		for(TIntIterator itr = positions.iterator(); itr.hasNext(); ) {
		  int position = itr.next();
			if(position < mentionStart || position > mentionEnd)
				return true;
		}
		return false;
	}
	
	public List<Integer> getPositions(int word, Mention mention) {
		int mentionStart = mention.getStartToken();
		int mentionEnd = mention.getEndToken();
		int mentionLength = mentionEnd - mentionStart + 1;
				
		List<Integer> positions = new LinkedList<Integer>();
		//we need to subtract the mention length if the keyword is after the mention
		for(int i = 0; i < indexIncludingStopWords.get(word).size(); i++) {
			//get the keyword position from the full index (including stopwords)
			int position = indexIncludingStopWords.get(word).get(i);
			//compare to know the position of the keyword relative to the mention
			if(position < mentionStart) //before the mention, return the actual position from the stopwords free index
				positions.add(indexWithoutStopWords.get(word).get(i));
			else if((position > mentionEnd)) //if after the mention, get the actual position and subtract mention length
				positions.add(indexWithoutStopWords.get(word).get(i) - mentionLength);
		}
		
		return positions;
	}
	
	public void addToIndex(TIntIntHashMap newIndexEntries) {
		for(int word: newIndexEntries.keys()) {
			int offset = newIndexEntries.get(word);
			
			TIntLinkedList positions;
			positions = indexIncludingStopWords.get(word); 
			if (positions == null) {
				positions = new TIntLinkedList();
				indexIncludingStopWords.put(word, positions);
			}
			positions.add(offset);
			
			positions = indexWithoutStopWords.get(word); 
			if (positions == null) {
				positions = new TIntLinkedList();
				indexWithoutStopWords.put(word, positions);
			}
			positions.add(offset);

			
		}		
	}

}
