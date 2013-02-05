package mpi.experiment.trace.data;

import java.util.LinkedList;
import java.util.List;

import mpi.experiment.trace.measures.MeasureTracer;

public class EntityTracer implements Comparable<EntityTracer> {

	private String entity;

	private double score;

	private List<MeasureTracer> measureTracers = new LinkedList<MeasureTracer>();

	public EntityTracer(String entity) {
		this.entity = entity;
	}

	public void addMeasureTracer(MeasureTracer mt) {
		measureTracers.add(mt);
	}

	public int compareTo(EntityTracer e) {
		return Double.compare(e.getTotalScore(), this.getTotalScore());
	}

	public String getName() {
		return entity;
	}

	public List<MeasureTracer> getMeasureTracers() {
		return measureTracers;
	}

	public double getTotalScore() {
		return score;
	}

	public void setTotalScore(double score) {
		this.score = score;
	}
}
