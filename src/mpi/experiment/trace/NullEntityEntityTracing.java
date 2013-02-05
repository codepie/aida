package mpi.experiment.trace;

import java.util.Collection;

import mpi.experiment.trace.measures.MeasureTracer;


public class NullEntityEntityTracing extends EntityEntityTracing {

  @Override
  public String generateOutput() {
    return "";
  }

  @Override
  public void addEntityEntityMeasureTracer(String e1, String e2, MeasureTracer mt) {
  }

  @Override
  public void setCorrectEntities(Collection<String> correctEntities) {
  }
}
