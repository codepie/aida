package mpi.experiment.reader;

import java.io.File;

public class KORE50Reader  extends AidaFormatCollectionReader {

	public final static String finalFileName = File.separator + "AIDA.tsv";

	public final static String finalCollectionPath = "./data/experiment/KORE50";

	public KORE50Reader(String collectionPath, String fileName) {
		super(collectionPath, fileName);
	}

	public KORE50Reader() {
		super(finalCollectionPath, finalFileName);
	}

	@Override
	protected int[] getCollectionPartFromTo(CollectionPart cp) {
		return new int[] { 1, 50 };
	}

}
