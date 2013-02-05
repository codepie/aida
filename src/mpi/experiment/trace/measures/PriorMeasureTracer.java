package mpi.experiment.trace.measures;

import java.text.DecimalFormat;

public class PriorMeasureTracer extends MeasureTracer {

	
	public PriorMeasureTracer(String name, double weight) {
		super(name, weight);
	}

	@Override
	public String getOutput() {
		DecimalFormat formatter = new DecimalFormat("#0.000");
		return "<strong style='color: #0000FF;'>score = " + formatter.format(score) + "</strong";
	}
	
}
