/*
** ██╗░░░██╗░██████╗███████╗██████╗░░█████╗░░██████╗███████╗
** ██║░░░██║██╔════╝██╔════╝██╔══██╗██╔══██╗██╔════╝██╔════╝
** ██║░░░██║╚█████╗░█████╗░░██████╦╝███████║╚█████╗░█████╗░░
** ██║░░░██║░╚═══██╗██╔══╝░░██╔══██╗██╔══██║░╚═══██╗██╔══╝░░
** ╚██████╔╝██████╔╝███████╗██████╦╝██║░░██║██████╔╝███████╗
** ░╚═════╝░╚═════╝░╚══════╝╚═════╝░╚═╝░░╚═╝╚═════╝░╚══════╝
*/
package io.doublegsoft.usebase.modelbase;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metamodel.ParameterizedObjectDefinition;
import com.doublegsoft.jcommons.metamodel.ReturnedObjectDefinition;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class ModelbaseWriter {

private final Writer writer;

  public ModelbaseWriter(Writer writer) {
    this.writer = writer;
  }

  public ModelbaseWriter write(ParameterizedObjectDefinition obj) throws IOException {
    writer.write("@request\n");
    String name = obj.getName().substring(1);
    name += "_request";
    writer.write(name);
    writer.write("<\n\n");
    int index = 0;
    int size = obj.getAttributes().length;
    for (AttributeDefinition attr : obj.getAttributes()) {
      if (attr.getLabelledOptions("original") != null &&
          attr.getLabelledOptions("original").get("attribute") != null) {
        writer.write("  ");
        writer.write("@original(");
        writer.write("object='");
        writer.write(attr.getLabelledOptions("original").get("object"));
        writer.write("', attribute='");
        writer.write(attr.getLabelledOptions("original").get("attribute"));
        writer.write("')\n");
      }
      writer.write("  ");
      writer.write(attr.getName());
      writer.write(": ");
      if (attr.getType() == null) {
        writer.write("string");
      } else {
        writer.write(attr.getType().getName());
      }
      if (index != size - 1) {
        writer.write(",");
      }
      writer.write("\n\n");
      index++;
    }
    writer.write(">\n\n");
    return this;
  }

  public ModelbaseWriter write(ReturnedObjectDefinition obj) throws IOException {
    if (obj.isArray()) {
      return this;
    }
    Map<String, String> original = obj.getLabelledOptions("original");
    if (original != null && original.get("object") != null) {
      return this;
    }
    writer.write("@response\n");
    String name = obj.getName().substring(1);
    name += "_response";
    writer.write(name);
    writer.write("<\n\n");
    int index = 0;
    int size = obj.getAttributes().length;
    for (AttributeDefinition attr : obj.getAttributes()) {
      writer.write("  ");
      if ("id".equals(attr.getName())) {
        writer.write(original.get("object") + "_" + attr.getName());
      } else {
        writer.write(attr.getName());
      }
      writer.write(": ");
      if (attr.getType() == null) {
        writer.write("string");
      } else {
        writer.write(attr.getType().getName());
      }
      if (index != size - 1) {
        writer.write(",");
      }
      writer.write("\n\n");
      index++;
    }
    writer.write(">\n\n");
    return this;
  }

  public ModelbaseWriter flush() throws IOException {
    writer.flush();
    return this;
  }

  public ModelbaseWriter close() throws IOException {
    writer.close();
    return this;
  }

}
