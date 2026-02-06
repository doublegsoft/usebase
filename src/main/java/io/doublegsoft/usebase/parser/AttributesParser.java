package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.StatementDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import com.doublegsoft.jcommons.metamodel.ValueDefinition;
import com.doublegsoft.jcommons.metamodel.ValuedAttributeDefinition;
import com.doublegsoft.jcommons.utils.Strings;
import io.doublegsoft.usebase.modelbase.ModelbaseHelper;

import java.util.HashMap;
import java.util.Map;

public class AttributesParser extends UsebaseParser {

  public AttributesParser(ModelDefinition dataModel) {
    super(dataModel);
  }

  public void assemble(io.doublegsoft.usebase.UsebaseParser.Usebase_attributesContext ctx,
                       ObjectDefinition owner, UsecaseDefinition usecase) {
    for (io.doublegsoft.usebase.UsebaseParser.Usebase_attributeContext ctxAttr : ctx.usebase_attribute()) {
      if (ctxAttr.usebase_attrgroup() != null) {
        for (io.doublegsoft.usebase.UsebaseParser.Anybase_idContext ctxId : ctxAttr.usebase_attrgroup().anybase_id()) {
          String attrname = ctxId.getText();
          AttributeDefinition attrDef = ModelbaseHelper.findAttributeByNames(owner, attrname, dataModel);

          AttributeDefinition attrInOwner = ModelbaseHelper.cloneAttribute(attrDef, owner);
          String groupName = "";
          for (io.doublegsoft.usebase.UsebaseParser.Anybase_idContext ctxIdInner : ctxAttr.usebase_attrgroup().anybase_id()) {
            groupName += "&" + ctxIdInner.getText();
          }
          attrInOwner.setLabelledOption("group", "name", groupName);
        }
        continue;
      }
      String attrname = ctxAttr.name.getText();
      AttributeDefinition attrInDataObj = ModelbaseHelper.findAttributeByNames(owner, attrname, dataModel);
      if (attrInDataObj == null) {
        String origObjName = owner.getLabelledOption("original", "object");
        attrInDataObj = ModelbaseHelper.findAttributeByNames(origObjName, attrname, dataModel);
      }
      if (attrInDataObj == null) {
        throw new RuntimeException("\"" + getOriginalText(ctx) + "\" has an attribute named \"" +
            ctxAttr.name.getText() + "\" not defined in data model.");
      }
      attrname = ctxAttr.alias != null ? ctxAttr.alias.getText() : attrInDataObj.getName();
      if (ModelbaseHelper.isSystemOrExistingInObject(attrname, owner)) {
        continue;
      }
      ValuedAttributeDefinition attrInOwner = (ValuedAttributeDefinition) ModelbaseHelper.cloneAttribute(
          attrname, attrInDataObj, owner);
      attrInOwner.setName(ModelbaseHelper.renameAttribute(attrInDataObj, ctxAttr.name.getText()));
      if (ctxAttr.usebase_validation() != null) {
        attrInOwner.getConstraint().setNullable(false);
      }
      if (ctxAttr.alias != null) {
        attrInOwner.setName(ctxAttr.alias.getText());
      }
      if (ctxAttr.value != null && !Strings.isEmpty(ctxAttr.value.getText())) {
        ValueDefinition value = new ValueDefinition();
        getValueParser().assemble(ctxAttr.value, value, usecase);
        attrInOwner.setValue(value);
        if (value.getNumber() != null) {
          attrInOwner.getConstraint().setDefaultValue(value.getNumber());
        } else if (value.getString() != null) {
          attrInOwner.getConstraint().setDefaultValue(value.getString());
        }
        attrInOwner.setValue(value);
      }
    }
  }

}
