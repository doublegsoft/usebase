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
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.*;
import com.doublegsoft.jcommons.metamodel.ParameterizedObjectDefinition;
import com.doublegsoft.jcommons.metamodel.ReturnedObjectDefinition;
import com.doublegsoft.jcommons.utils.Strings;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class ModelbaseWriter {

  private final Writer writer;

  private final ModelDefinition dataModel;

  public ModelbaseWriter(Writer writer, ModelDefinition dataModel) {
    this.writer = writer;
    this.dataModel = dataModel;
  }

  public ModelbaseWriter write(ParameterizedObjectDefinition obj) throws IOException {
    writer.write("@request\n");
    for (AttributeDefinition attr : obj.getAttributes()) {
      if (attr.isLabelled("original")) {
        String origObjName = attr.getLabelledOption("original", "object");
        ObjectDefinition dataObj = dataModel.findObjectByName(origObjName);
        writer.write("@name(label='");
        writer.write(dataObj.getLabelledOption("name", "label") + "为主体请求对象");
        writer.write("')\n");
        break;
      }
    }
    String name = obj.getName().substring(1);
    name += "_params";
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
//      return this;
    }
    writer.write("@response\n");
    for (AttributeDefinition attr : obj.getAttributes()) {
      if (attr.isLabelled("original")) {
        String origObjName = attr.getLabelledOption("original", "object");
        String origAttrName = attr.getLabelledOption("original", "attribute");
        AttributeDefinition origAttr = dataModel.findAttributeByNames(origObjName, origAttrName);
        writer.write("@name(label='");
        writer.write(origAttr.getParent().getLabelledOption("name", "label") + "为主体返回对象");
        writer.write("')\n");
        break;
      }
    }
    String name = obj.getName().substring(1);
    name += "_result";
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

  public ModelbaseWriter write(ObjectDefinition obj) throws IOException {
    if (obj == null) {
      return this;
    }
    writer.write("@row\n");
    for (AttributeDefinition attr : obj.getAttributes()) {
      if (attr.isLabelled("original")) {
        String origObjName = attr.getLabelledOption("original", "object");
        String origAttrName = attr.getLabelledOption("original", "attribute");
        AttributeDefinition origAttr = dataModel.findAttributeByNames(origObjName, origAttrName);
        writer.write("@name(label='");
        writer.write(origAttr.getParent().getLabelledOption("name", "label")+ "的宽对象");
        writer.write("')\n");
        break;
      }
    }
    String name = obj.getName();
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
    String origObjName4Attr = null;
    String origAttrName4Attr = null;
    String origObjName4Obj = null;
    if (attr.getLabelledOptions("original") != null &&
        attr.getLabelledOptions("original").get("attribute") != null) {
      Map<String, String> original = attr.getLabelledOptions("original");
      origObjName4Attr = original.get("object");
      origAttrName4Attr = original.get("attribute");
      ObjectDefinition origObj = dataModel.findObjectByName(origObjName4Attr);
      AttributeDefinition origAttr = dataModel.findAttributeByNames(origObjName4Attr, origAttrName4Attr);
      writer.write("  ");
      writer.write("@original(");
      writer.write("object='");
      writer.write(origObjName4Attr);
      writer.write("', attribute='");
      writer.write(origAttrName4Attr);
      writer.write("')\n");
      if (origAttr != null) {
        writer.write("  ");
        writer.write("@name(label='");
        writer.write(origObj.getLabelledOption("name", "label"));
        writer.write("-");
        writer.write(origAttr.getLabelledOption("name", "label"));
        writer.write("')\n");
      }
    }
    annotateConjunctionForAttribute(attr);
    if (origObjName4Attr == null && attr.getParent().isLabelled("original")) {
      origObjName4Obj = attr.getParent().getLabelledOptions("original").get("object");
    }
    writer.write("  ");
    if ("id".equals(attr.getName()) ||
        "name".equals(attr.getName())  ||
        "type".equals(attr.getName()) ||
        "code".equals(attr.getName())) {
      if (origObjName4Attr != null) {
        writer.write(origObjName4Attr + "_" + attr.getName());
      } else if (origObjName4Obj != null) {
        writer.write("@original(");
        writer.write("object='");
        writer.write(origObjName4Obj);
        writer.write("', attribute='");
        writer.write(attr.getName());
        writer.write("')\n");
        writer.write("  ");
        writer.write(origObjName4Obj + "_" + attr.getName());
      } else {
        writer.write(attr.getName());
      }
    } else if (attr.getType().isCustom()) {
      if (attr.getType().getName().equals(attr.getName())) {
        writer.write(attr.getName() +  "_id");
      } else {
        writer.write(attr.getName());
//        throw new UnsupportedOperationException("还没有支持");
      }
    } else {
      writer.write(attr.getName());
    }
    if (attr.getConstraint().isIdentifiable()) {
      writer.write("!!");
    }
    if (!attr.getConstraint().isNullable() && !attr.getConstraint().isIdentifiable()) {
      writer.write("!");
    }
    writer.write(": ");
    writeObjectType(attr.getType());
  }

  private void writeObjectType(ObjectType type) throws IOException {
    if (type == null) {
      writer.write("string");
    } else if (type instanceof PrimitiveType) {
      writer.write(type.getName().toLowerCase());
    } else if (type instanceof CollectionType) {
      writer.write("&" + ((CollectionType) type).getComponentType().getName() + "_row[]");
    } else if (type instanceof CustomType) {
      writer.write("long");
    } else if (type instanceof DomainType) {
      writer.write("string");
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
      if (origObjNameInAttr == null || !origObjNameInAttr.equals(origObjName)) {
        return false;
      }
    }
    return true;
  }

  private void annotateConjunctionForAttribute(AttributeDefinition attr) throws IOException {
    annotateConjunctionForAttribute(attr, 0);
    for (int i = 1; i <= 9; i++) {
      annotateConjunctionForAttribute(attr, i);
    }
  }

  private void annotateConjunctionForAttribute(AttributeDefinition attr, int index) throws IOException {
    Map<String, String> conjunction = null;
    boolean appendedAttribute = false;
    if (index <= 0) {
      conjunction = attr.getLabelledOptions("conjunction");
    } else {
      conjunction = attr.getLabelledOptions("conjunction_" + index);
    }
    if (conjunction != null && !conjunction.isEmpty()) {
      writer.write("  ");
      writer.write("@conjunction(");
      if (conjunction.containsKey("name")) {
        writer.write("name='");
        writer.write(conjunction.get("name"));
        writer.write("'");
        appendedAttribute = true;
      }
      if (conjunction.containsKey("source_object")) {
        if (appendedAttribute) {
          writer.write(", ");
        }
        writer.write("source_object='");
        writer.write(conjunction.get("source_object"));
        writer.write("'");
        appendedAttribute = true;
      }
      if (conjunction.containsKey("source_attribute")) {
        if (appendedAttribute) {
          writer.write(", ");
        }
        writer.write("source_attribute='");
        writer.write(conjunction.get("source_attribute"));
        writer.write("'");
        appendedAttribute = true;
      }
      if (conjunction.containsKey("target_object")) {
        if (appendedAttribute) {
          writer.write(", ");
        }
        writer.write("target_object='");
        writer.write(conjunction.get("target_object"));
        writer.write("'");
        appendedAttribute = true;
      }
      if (conjunction.containsKey("target_attribute")) {
        if (appendedAttribute) {
          writer.write(", ");
        }
        writer.write("target_attribute='");
        writer.write(conjunction.get("target_attribute"));
        writer.write("'");
        appendedAttribute = true;
      }
      writer.write(")\n");
    }
  }
}
