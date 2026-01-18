package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

public abstract class UsebaseParser {

  protected final ModelDefinition dataModel;

  public UsebaseParser(ModelDefinition dataModel) {
    this.dataModel = dataModel;
  }

  protected String getOriginalText(ParserRuleContext ctx) {
    Interval intv = new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
    return ctx.start.getInputStream().getText(intv);
  }

  protected AttributeDefinition findAttributeInDataModel(String expr) {
    String[] names = expr.split("\\.");
    if (names.length == 1) {
      return null;
    }
    return dataModel.findAttributeByNames(names[0], names[1]);
  }

  protected AttributeDefinition findAttributeInDataModel(ObjectDefinition owner, String attrName) {
    String objName = owner.getName();
    if (!Character.isAlphabetic(objName.charAt(0))) {
      objName = objName.substring(1);
    }
    String origObjName = owner.getLabelledOption("original", "object");
    ObjectDefinition obj = dataModel.findObjectByName(objName);
    if (obj == null) {
      obj = dataModel.findObjectByName(origObjName);
    }
    if (obj != null) {
      objName = origObjName;
    }
    AttributeDefinition retVal = dataModel.findAttributeByNames(objName, attrName);
    if (retVal == null) {
      if (attrName.startsWith(objName + "_")) {
        String newAttrName = attrName.replaceAll(objName + "_", "");
        retVal = dataModel.findAttributeByNames(objName, newAttrName);
      }
    }
    if (retVal == null) {
      String newAttrName = attrName.replaceAll("_id", "");
      retVal = dataModel.findAttributeByNames(objName, newAttrName);
    }
    if (retVal == null) {
      throw new IllegalArgumentException("not found '" + objName + "." + attrName + "'in data model");
    }
    return retVal;
  }

}
