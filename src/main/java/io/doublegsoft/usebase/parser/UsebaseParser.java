package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

public abstract class UsebaseParser {

  protected final ModelDefinition dataModel;

  private AggregateParser aggregateParser;

  private ArrayParser arrayParser;

  private ConditionsParser conditionsParser;

  private ArgumentsParser argumentsParser;

  private AttributesParser attributesParser;

  private CalcExprParser calcExprParser;

  private ObjectParser objectParser;

  private ValueParser valueParser;

  private SourceParser sourceParser;

  public UsebaseParser(ModelDefinition dataModel) {
    this.dataModel = dataModel;
  }

  protected AttributeDefinition findAttributeInDataModel(String expr) {
    String[] names = expr.split("\\.");
    if (names.length == 1) {
      return null;
    }
    ObjectDefinition obj = dataModel.findObjectByName(names[0]);
    if (obj == null) {
      return null;
    }
    String attrname = names[1];
    for (AttributeDefinition attr : obj.getAttributes()) {
      if (attr.getName().equals(attrname)) {
        return attr;
      }
      if (attr.getType().isCustom()) {
        ObjectDefinition refObj = dataModel.findObjectByName(attr.getType().getName());
        AttributeDefinition idAttrRefObj = refObj.getIdentifiableAttribute();
        if (idAttrRefObj.getName().equals(attrname) ||
            (refObj.getName() + "_" + idAttrRefObj.getName()).equals(attrname)) {
          return attr;
        }
      }
    }
    return null;
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
    AttributeDefinition retVal = dataModel.findAttributeByNames(objName, attrName);
    if (retVal == null) {
      if (attrName.startsWith(objName + "_")) {
        String newAttrName = attrName.replaceAll(objName + "_", "");
        retVal = dataModel.findAttributeByNames(objName, newAttrName);
      }
    }
    if (retVal == null) {
      String newAttrName = attrName.replace(owner.getName() + "_", "");
      retVal = dataModel.findAttributeByNames(objName, newAttrName);
    }
    return retVal;
  }

  protected AggregateParser getAggregateParser() {
    if (aggregateParser == null) {
      aggregateParser = new AggregateParser(dataModel);
    }
    return aggregateParser;
  }

  protected ArrayParser getArrayParser() {
    if (arrayParser == null) {
      arrayParser = new ArrayParser(dataModel);
    }
    return arrayParser;
  }

  protected ConditionsParser getConditionsParser() {
    if (conditionsParser == null) {
      conditionsParser = new ConditionsParser(dataModel);
    }
    return conditionsParser;
  }

  protected ArgumentsParser getArgumentsParser() {
    if (argumentsParser == null) {
      argumentsParser = new ArgumentsParser(dataModel);
    }
    return argumentsParser;
  }

  protected AttributesParser getAttributesParser() {
    if (attributesParser == null) {
      attributesParser = new AttributesParser(dataModel);
    }
    return attributesParser;
  }

  protected CalcExprParser getCalcExprParser() {
    if (calcExprParser == null) {
      calcExprParser = new CalcExprParser(dataModel);
    }
    return calcExprParser;
  }

  protected ObjectParser getObjectParser() {
    if (objectParser == null) {
      objectParser = new ObjectParser(dataModel);
    }
    return objectParser;
  }

  protected ValueParser getValueParser() {
    if (valueParser == null) {
      valueParser = new ValueParser(dataModel);
    }
    return valueParser;
  }

  protected SourceParser getSourceParser() {
    if (sourceParser == null) {
      sourceParser = new SourceParser(dataModel);
    }
    return sourceParser;
  }

  public static String getOriginalText(ParserRuleContext ctx) {
    Interval intv = new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
    return ctx.start.getInputStream().getText(intv);
  }
}
