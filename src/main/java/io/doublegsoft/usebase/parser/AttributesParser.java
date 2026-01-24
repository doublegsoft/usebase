package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.StatementDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import com.doublegsoft.jcommons.metamodel.ValueDefinition;
import com.doublegsoft.jcommons.metamodel.ValuedAttributeDefinition;
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
          AttributeDefinition attrDef = findAttributeInDataModel(owner, attrname);// dataModel.findAttributeByNames(owner.getName(), attrname);
          if (attrDef == null) {
            attrDef = dataModel.findAttributeByNames(owner.getName(), attrname.replace(owner.getName() + "_", ""));
          }
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
      AttributeDefinition attrInDataObj = findAttributeInDataModel(owner, attrname);
      if (attrInDataObj == null) {
        String origObjName = owner.getLabelledOption("original", "object");
        attrInDataObj = dataModel.findAttributeByNames(origObjName, attrname);
      }
      if (attrInDataObj == null) {
        throw new RuntimeException("\"" + getOriginalText(ctx) + "\" has an attribute named \"" +
            ctxAttr.name.getText() + "\" not defined in data model.");
      }
      if (ModelbaseHelper.isSystemOrExistingInObject(attrInDataObj.getName(), owner)) {
        continue;
      }
      ValuedAttributeDefinition attrInOwner = (ValuedAttributeDefinition) ModelbaseHelper.cloneAttribute(attrInDataObj, owner);
      if (ctxAttr.usebase_validation() != null) {
        attrInOwner.getConstraint().setNullable(false);
      }
      if (ctxAttr.alias != null) {
        attrInOwner.setName(ctxAttr.alias.getText());
      }
      if (ctxAttr.value != null) {
        ValueDefinition value = new ValueDefinition();
        getValueParser().assemble(ctxAttr.value, value, usecase);
      }
    }
  }

}
