package io.doublegsoft.usebase.dot;

import java.util.ArrayList;
import java.util.List;

public class DotGraph extends DotVertex {

  private String name;

  private boolean directed;

  private final List<DotVertex> vertices = new ArrayList<>();

  private final List<DotEdge> edges = new ArrayList<>();

  private final List<DotGraph> subgraphs = new ArrayList<>();

  public void addEdge(DotEdge edge) {
    boolean foundStart = false;
    boolean foundEnd = false;
    for (DotVertex vertex : vertices) {
      if (vertex.equals(edge.getStart())) {
        foundStart = true;
      }
      if (vertex.equals(edge.getEnd())) {
        foundEnd = true;
      }
    }
    if (!foundStart) {
      vertices.add(edge.getStart());
    }
    if (!foundEnd) {
      vertices.add(edge.getEnd());
    }
    boolean foundEdge = false;
    for (DotEdge e : edges) {
      if (e.equals(edge)) {
        foundEdge = true;
      }
    }
    if (!foundEdge) {
      edges.add(edge);
    }
  }

  public void addSubgraph(DotGraph subgraph) {
    subgraphs.add(subgraph);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isDirected() {
    return directed;
  }

  public void setDirected(boolean directed) {
    this.directed = directed;
  }

  public List<DotVertex> getVertices() {
    return vertices;
  }

  public List<DotEdge> getEdges() {
    return edges;
  }

  @Override
  public String toString() {
    return toString(0, directed ? "digraph" : "graph");
  }

  private String toString(int indent, String type) {
    StringBuilder retVal = new StringBuilder();
    indent(retVal, indent);
    retVal.append(type).append(" ").append(name).append(" {\n");
    for (DotGraph subgraph : subgraphs) {
      retVal.append(subgraph.toString(indent + 2, "subgraph")).append("\n");
    }
    for (DotVertex vertex : vertices) {
      indent(retVal, indent + 2);
      retVal.append(vertex.getId()).append(" [");
      retVal.append("label=\"").append(vertex.getLabel()).append("\"");
      retVal.append("]\n");
    }
    for (DotEdge edge : edges) {
      indent(retVal, indent + 2);
      retVal.append(edge.getStart().getId());
      retVal.append(directed ? " -> " : " -- ");
      retVal.append(edge.getEnd().getId());
      retVal.append(";\n");
    }
    indent(retVal, indent);
    retVal.append("}");
    return retVal.toString();
  }

  private void indent(StringBuilder builder, int indent) {
    for (int i = 0; i < indent; i++) {
      builder.append(" ");
    }
  }
}
