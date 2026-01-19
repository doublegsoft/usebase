package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.CollectionType;
import com.doublegsoft.jcommons.metabean.type.CustomType;
import com.doublegsoft.jcommons.metamodel.ReturnedObjectDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import com.doublegsoft.jcommons.utils.Inflector;
import io.doublegsoft.usebase.modelbase.ModelbaseHelper;

import java.util.Arrays;

public class ArrayParser extends UsebaseParser {

  private final ArgumentsParser argumentsParser;

  private final AggregateParser aggregateParser;

  public ArrayParser(ModelDefinition dataModel) {
    super(dataModel);
    argumentsParser = new ArgumentsParser(dataModel);
    aggregateParser = new AggregateParser(dataModel);
  }

  public void assemble(io.doublegsoft.usebase.UsebaseParser.Usebase_arrayContext ctx,
                       ObjectDefinition owner, UsecaseDefinition usecase) {
    String attrName = null;
    CollectionType attrType = new CollectionType(getOriginalText(ctx));
    if (ctx.alias != null) {
      attrName = ctx.alias.getText();
    }
    if (ctx.usebase_aggregate() != null) {
      if (owner instanceof ReturnedObjectDefinition) {
        ((ReturnedObjectDefinition)owner).setArray(true);
        getAggregateParser().assemble(ctx.usebase_aggregate(), owner, usecase);
      } else {
        io.doublegsoft.usebase.UsebaseParser.Usebase_aggregateContext ctxAgg = ctx.usebase_aggregate();
        if (!ctxAgg.usebase_data().isEmpty()) {
          if (ctxAgg.usebase_data().size() == 1) {
            io.doublegsoft.usebase.UsebaseParser.Usebase_objectContext ctxObj = ctxAgg.usebase_data().get(0).usebase_object();
            if (ctxObj == null) {
              throw new RuntimeException("usebase_object not found in array rule");
            }
            String aggObjName = ctxAgg.usebase_data().get(0).usebase_object().name.getText();
            ObjectDefinition originalObjDef = dataModel.findObjectByName(aggObjName);
            ObjectDefinition propagatedObjDef = new ObjectDefinition(aggObjName, usecase.getContextModel());
            attrType.setComponentType(propagatedObjDef);
            if (ctxObj.usebase_attributes() != null) {
              AttributesParser attrsParser = new AttributesParser(dataModel);
              attrsParser.assemble(ctxObj.usebase_attributes(), owner);
            } else {
              if (!ModelbaseHelper.isSystemOrExistingInObject(originalObjDef.getName(), propagatedObjDef)) {
                ModelbaseHelper.cloneAttributes(Arrays.asList(originalObjDef.getAttributes()), propagatedObjDef);
              }
            }
            if (ctx.usebase_source() != null) {
              ModelbaseHelper.addOptions(owner, "original", "source",
                  ctx.usebase_source().anybase_identifier().getText());
            }
            if (ctx.usebase_arguments() != null) {
              String objName = "$";
              if (owner.getName().startsWith("#")) {
                objName += owner.getName().substring(1);
              } else if (owner.getName().startsWith("$")) {
                objName += owner.getName().substring(1);
              } else {
                objName += owner.getName();
              }
              if (owner.getName().startsWith("$")) {
                argumentsParser.assemble(ctx.usebase_arguments(), owner, usecase);
              } else {
                ObjectDefinition argsObj = new ObjectDefinition(objName, usecase.getContextModel());
                argumentsParser.assemble(ctx.usebase_arguments(), argsObj, usecase);
              }
            }
          } else {
            if (ctx.usebase_source() != null) {
              throw new RuntimeException("multi-objects aggregate with source not allowed");
            }
            ObjectDefinition aggObj = new ObjectDefinition("[]" + owner.getName(), usecase.getContextModel());
            aggregateParser.assemble(ctx.usebase_aggregate(), aggObj, usecase);
            attrType.setComponentType(aggObj);
          }
        }
      }
    } else if (ctx.name != null) {
      ObjectDefinition objInDataModel = dataModel.findObjectByName(ctx.name.getText());
      if (objInDataModel == null) {
        throw new RuntimeException("object named \"" +ctx.name.getText() + "\" not found in data model");
      }
      if (attrName == null) {
        attrName = Inflector.getInstance().pluralize(objInDataModel.getName());
      }
      if (owner instanceof ReturnedObjectDefinition) {
        for (AttributeDefinition attrDef : objInDataModel.getAttributes()) {
          if (!ModelbaseHelper.isSystemOrExistingInObject(attrDef.getName(), owner)) {
            ModelbaseHelper.cloneAttribute(attrDef, owner);
          }
        }
      }
      attrType.setComponentType(new CustomType(objInDataModel.getName(), objInDataModel));
      if (ctx.usebase_source() != null) {
        ModelbaseHelper.addOptions(owner, "original", "source",
            ctx.usebase_source().anybase_identifier().getText());
      }
      if (ctx.usebase_arguments() != null) {
        getArgumentsParser().assembleOrCreateAndThen(ctx.usebase_arguments(), owner, usecase);
      }
    }
    if (!(owner instanceof ReturnedObjectDefinition)) {
      AttributeDefinition collAttr = new AttributeDefinition(attrName, owner);
      collAttr.setLabelledOption("original", "object", attrType.getComponentType().getName());
      collAttr.setLabelledOption("original", "array", "true");
      collAttr.setType(attrType);
    }
  }
}
