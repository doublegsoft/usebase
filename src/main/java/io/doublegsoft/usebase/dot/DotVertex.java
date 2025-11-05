package io.doublegsoft.usebase.dot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DotVertex {

  private String id;

  private String label;

  private final Map<String, String> options = new HashMap<>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    DotVertex dotVertex = (DotVertex) o;
    return Objects.equals(id, dotVertex.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
