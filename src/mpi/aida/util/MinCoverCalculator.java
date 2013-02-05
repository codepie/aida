package mpi.aida.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.buffer.PriorityBuffer;

public class MinCoverCalculator {

	public MinCover calculateMinCover(List<List<Integer>> positions) {
		MinCover minCover = new MinCover();
		if (positions == null) {
			minCover.length = -1;
			return minCover;
		}
		if (positions.size() < 1) {
			minCover.length = -1;
			return minCover;
		}
		if (positions.size() == 1) {
			minCover.length = 1;
			minCover.startPositions = positions.get(0);
			minCover.endPositions = positions.get(0);
			return minCover;
		}

		PriorityBuffer occurrences = new PriorityBuffer(
				new SymbolOccurrenceComparator());
		int symbolsCount = positions.size();
		for (int i = 0; i < symbolsCount; i++) {
			ListIterator<Integer> symbolPositionsIterator = positions.get(i)
					.listIterator();
			while (symbolPositionsIterator.hasNext())
				occurrences.add(new SymbolOccurrence(i, symbolPositionsIterator
						.next()));

		}

		// prepare a list to hold the currently select occurrences
		int coveredOccurrences[] = new int[symbolsCount];
		for (int i = 0; i < symbolsCount; i++) {
			coveredOccurrences[i] = -1;
		}

		int coveredSymbolsCount = 0;
		SymbolOccurrence symbolOccurrence;
		while (coveredSymbolsCount < symbolsCount) {
			// keep taking symbols as long as not all symbols are covered
			symbolOccurrence = (SymbolOccurrence) occurrences.remove();
			if (coveredOccurrences[symbolOccurrence.id] == -1) {// new symbol
																// got covered
				coveredSymbolsCount++;
			}
			coveredOccurrences[symbolOccurrence.id] = symbolOccurrence.position;
		}

		// hold the min and max selected occurrence to easily calculate the
		// cover length
		int minSelectOccurence = coveredOccurrences[0];
		int maxSelectOccurence = coveredOccurrences[0];

		for (int i = 1; i < symbolsCount; i++) {
			minSelectOccurence = Math.min(minSelectOccurence,
					coveredOccurrences[i]);
			maxSelectOccurence = Math.max(maxSelectOccurence,
					coveredOccurrences[i]);
		}
		int minCoverLength = maxSelectOccurence - minSelectOccurence + 1;
		minCover.length = minCoverLength;
		minCover.startPositions = new ArrayList<Integer>();
		minCover.startPositions.add(minSelectOccurence);
		minCover.endPositions = new ArrayList<Integer>();
		minCover.endPositions.add(maxSelectOccurence);


		int coverLength;
		while (minCoverLength > symbolsCount && !occurrences.isEmpty()) {
			symbolOccurrence = (SymbolOccurrence) occurrences.remove();
			maxSelectOccurence = symbolOccurrence.position;
			// if the new symbol was the minimum occurrence before, then min
			// needs to be updated as well
			if (coveredOccurrences[symbolOccurrence.id] == minSelectOccurence) {
				coveredOccurrences[symbolOccurrence.id] = symbolOccurrence.position;
				minSelectOccurence = coveredOccurrences[0];
				for (int i = 1; i < symbolsCount; i++) {
					minSelectOccurence = Math.min(minSelectOccurence,
							coveredOccurrences[i]);
				}
			} else { // otherwise, just update the occurrences array
				coveredOccurrences[symbolOccurrence.id] = symbolOccurrence.position;
			}
			coverLength = maxSelectOccurence - minSelectOccurence + 1;
			if (coverLength < minCoverLength) {
				minCoverLength = coverLength;
				minCover.length = coverLength;
				minCover.startPositions = new ArrayList<Integer>();
				minCover.startPositions.add(minSelectOccurence);
				minCover.endPositions = new ArrayList<Integer>();
				minCover.endPositions.add(maxSelectOccurence);
			} else if (coverLength == minCoverLength) {
				minCover.startPositions.add(minSelectOccurence);
				minCover.endPositions.add(maxSelectOccurence);
			}
		}
		return minCover;
	}

	private class SymbolOccurrence {
		private int id;
		private int position;

		public SymbolOccurrence(int id, int index) {
			super();
			this.id = id;
			this.position = index;
		}

	}

	private class SymbolOccurrenceComparator implements
			Comparator<SymbolOccurrence> {

		@Override
		public int compare(SymbolOccurrence o1, SymbolOccurrence o2) {
			if (o1.position < o2.position)
				return -1;
			else if (o1.position == o2.position)
				return 0;
			else if (o1.position > o2.position)
				return 1;
			return 0;
		}

	}

}
