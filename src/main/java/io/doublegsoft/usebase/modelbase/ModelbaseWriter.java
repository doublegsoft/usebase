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
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.CollectionType;
import com.doublegsoft.jcommons.metabean.type.ObjectType;
import com.doublegsoft.jcommons.metabean.type.PrimitiveType;
import com.doublegsoft.jcommons.metamodel.ParameterizedObjectDefinition;
import com.doublegsoft.jcommons.metamodel.ReturnedObjectDefinition;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
      writeAttribute(attr);
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
    if (obj == null) {
      return this;
    }
    Map<String, String> original = obj.getLabelledOptions("original");
    if (original != null && isOnlyIncludingOriginalAttributes(obj)) {
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
      writeAttribute(attr);
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

  private void writeAttribute(AttributeDefinition attr) throws IOException {
    String origObj4Attr = null;
    String origAttr4Attr = null;
    String origObj4Obj = null;
    if (attr.getLabelledOptions("original") != null &&
        attr.getLabelledOptions("original").get("attribute") != null) {
      origObj4Attr = attr.getLabelledOptions("original").get("object");
      origAttr4Attr = attr.getLabelledOptions("original").get("attribute");
      writer.write("  ");
      writer.write("@original(");
      writer.write("object='");
      writer.write(origObj4Attr);
      writer.write("', attribute='");
      writer.write(origAttr4Attr);
      writer.write("')\n");
    }
    if (origObj4Attr == null && attr.getParent().isLabelled("original")) {
      origObj4Obj = attr.getParent().getLabelledOptions("original").get("object");
    }
    writer.write("  ");
    if ("id".equals(attr.getName()) || "name".equals(attr.getName())) {
      if (origObj4Attr != null) {
        writer.write(origObj4Attr + "_" + attr.getName());
      } else if (origObj4Obj != null) {
        writer.write("@original(");
        writer.write("object='");
        writer.write(origObj4Obj);
        writer.write("', attribute='");
        writer.write(attr.getName());
        writer.write("')\n");
        writer.write("  ");
        writer.write(origObj4Obj + "_" + attr.getName());
      } else {
        writer.write(attr.getName());
      }
    } else {
      writer.write(attr.getName());
    }
    writer.write(": ");
    writeObjectType(attr.getType());
  }

  private void writeObjectType(ObjectType type) throws IOException {
    if (type == null) {
      writer.write("string");
    } else if (type instanceof PrimitiveType) {
      writer.write(type.getName());
    } else if (type instanceof CollectionType) {
      writer.write("&" + ((CollectionType) type).getComponentType().getName() + "[]");
    }
  }

  private boolean isOnlyIncludingOriginalAttributes(ObjectDefinition obj) {
    if (!obj.isLabelled("original")) {
      return false;
    }
    Map<String,String> original = obj.getLabelledOptions("original");
    String origObjName = original.get("object");
    for (AttributeDefinition attr : obj.getAttributes()) {
      if (!attr.isLabelled("original")) {
        return false;
      }
      String origObjNameInAttr = attr.getLabelledOptions("original").get("object");
      if (!origObjNameInAttr.equals(origObjName)) {
        return false;
      }
    }
    return true;
  }

}
