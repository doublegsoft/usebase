/*
 ** ████████████████████████████████████████████
 ** █▄─██─▄█─▄▄▄▄█▄─▄▄─█▄─▄─▀██▀▄─██─▄▄▄▄█▄─▄▄─█
 ** ██─██─██▄▄▄▄─██─▄█▀██─▄─▀██─▀─██▄▄▄▄─██─▄█▀█
 ** ▀▀▄▄▄▄▀▀▄▄▄▄▄▀▄▄▄▄▄▀▄▄▄▄▀▀▄▄▀▄▄▀▄▄▄▄▄▀▄▄▄▄▄▀
 */
package io.doublegsoft.usebase.dot;

import com.doublegsoft.jcommons.metamodel.StatementDefinition;

import java.util.ArrayList;
import java.util.List;

public class DotBuilder {

  /**
   * Converts statements of usecase to dot graph language at
   * <a href="https://graphviz.org/doc/info/lang.html">DOT Language</a>.
   *
   * @param name
   *      the usecase name
   *
   * @param startIndex
   *      the index as node id
   *
   * @param statements
   *      the statements
   *
   * @return dot graph language expression
   */
  public DotGraph build(String name, int startIndex, List<StatementDefinition> statements) {
    DotGraph retVal = new DotGraph();
    retVal.setName(name);
    retVal.setDirected(true);
    int index = startIndex;
    List<DotVertex> vertices = new ArrayList<>();
    for (StatementDefinition stmt : statements) {
      DotVertex vertex = new DotVertex();
      vertex.setLabel(stmt.getOriginalText());
      vertex.setId(index + "");
      vertices.add(vertex);
      index++;
      if (!stmt.getStatements().isEmpty()) {
        DotGraph subgraph = build(name + "_" + index, index, stmt.getStatements());
        retVal.addSubgraph(subgraph);
        vertices.add(subgraph.getVertices().get(0));
      }
    }
    for (int i = 0; i < vertices.size() - 1; i++) {
      DotEdge edge = new DotEdge(vertices.get(i), vertices.get(i + 1));
      retVal.addEdge(edge);
    }
    return retVal;
  }

}
