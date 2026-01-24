package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.modelbase.ModelbaseHelper;

public class ObjectParser extends UsebaseParser {

  public ObjectParser(ModelDefinition dataModel) {
    super(dataModel);
  }

  public void assemble(io.doublegsoft.usebase.UsebaseParser.Usebase_objectContext ctx, ObjectDefinition owner, UsecaseDefinition usecase) {
    if (ctx.usebase_attributes() != null) {
      for (io.doublegsoft.usebase.UsebaseParser.Usebase_attributeContext ctxAttr : ctx.usebase_attributes().usebase_attribute()) {
        AttributeDefinition attrDef = dataModel.findAttributeByNames(ctx.name.getText(), ctxAttr.name.getText());
        if (attrDef == null) {
          throw new RuntimeException("\"" + getOriginalText(ctx) + "\" has an attribute named \"" +
              ctxAttr.name.getText() + "\" not defined in data model.");
        }
        if (!ModelbaseHelper.isSystemOrExistingInObject(attrDef.getName(), owner)) {
          AttributeDefinition attr = ModelbaseHelper.cloneAttribute(attrDef, owner);
          if (ctx.alias != null) {
            attr.setLabelledOption("alias", "object", ctx.alias.getText());
          }
        }
      }
    } else {
      ObjectDefinition originalObj = dataModel.findObjectByName(ctx.name.getText());
      for (AttributeDefinition attrDef : originalObj.getAttributes()) {
        if (!ModelbaseHelper.isSystemOrExistingInObject(attrDef.getName(), owner)) {
          AttributeDefinition attr = ModelbaseHelper.cloneAttribute(attrDef, owner);
          if (ctx.alias != null) {
            attr.setLabelledOption("alias", "object", ctx.alias.getText());
          }
        }
      }
    }
    if (ctx.usebase_source() != null) {
      ModelbaseHelper.addOptions(owner, "original", "source",
          ctx.usebase_source().anybase_identifier().getText());
    }
    if (ctx.usebase_arguments() != null) {
      getArgumentsParser().assembleOrCreateAndThen(ctx.usebase_arguments(), owner, usecase);
    }
  }
}
