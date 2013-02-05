package mpi.experiment.trace;

import java.util.List;
import java.util.Map;

public class NullGraphTracer extends GraphTracer {

	@Override
	public void addCandidateEntityToOriginalGraph(String docId, String mention,
			String candidateEntity, double entityWeightedDegree, double MESimilairty, Map<String, Double> connectedEntities) {
	}

	@Override
	public void addCandidateEntityToCleanedGraph(String docId, String mention,
			String candidateEntity, double entityWeightedDegree, double MESimilairty) {
	}

	@Override
	public void addCandidateEntityToFinalGraph(String docId, String mention,
			String candidateEntity, double entityWeightedDegree, double MESimilairty) {
	}

	@Override
	public void addEntityRemovalStep(String docId, String entity, double entityWeightedDegree, List<String> connectedMentions) {
	}

	@Override
	public void writeOutput(String outputPath) {
	}

	 public void addStat(String docId, String description, String value) {
	 }
}
