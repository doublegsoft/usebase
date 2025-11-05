package io.doublegsoft.usebase.dot;

import java.util.HashMap;
import java.util.Map;

public class DotEdge {

  private final DotVertex start;

  private final DotVertex end;

  private String label;

  private final Map<String, String> options = new HashMap<>();

  public DotEdge(DotVertex start, DotVertex end) {
    this.start = start;
    this.end = end;
  }

  public DotVertex getStart() {
    return start;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public DotVertex getEnd() {
    return end;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    DotEdge dotEdge = (DotEdge) o;
    return start.equals(dotEdge.start) && end.equals(dotEdge.end);
  }

  @Override
  public int hashCode() {
    int result = start.hashCode();
    result = 31 * result + end.hashCode();
    return result;
  }
}
