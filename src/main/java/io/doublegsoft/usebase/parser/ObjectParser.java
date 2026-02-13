package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.PrimitiveType;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.modelbase.ModelbaseHelper;

public class ObjectParser extends UsebaseParser {

  public ObjectParser(ModelDefinition dataModel) {
    super(dataModel);
  }

  public void assemble(io.doublegsoft.usebase.UsebaseParser.Usebase_objectContext ctx, ObjectDefinition owner, UsecaseDefinition usecase) {
    if (ctx.usebase_attributes() != null) {
      owner.setLabelledOption("original", "object", ctx.name.getText());
      getAttributesParser().assemble(ctx.usebase_attributes(), owner, usecase);
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
      getArgumentsParser().assembleOrCreateAndThen(ctx.usebase_arguments(), true, owner, usecase);
      ObjectDefinition argsObj = usecase.getContextModel().findObjectByName("#" + getOriginalText(ctx.usebase_arguments()));
      for (AttributeDefinition argAttr : argsObj.getAttributes()) {
        String attrname = argAttr.getName();
        String dfltVal = "";
        String objname = "";
        String[] strs = attrname.split("\\.");
        if (strs.length == 1) {
          attrname = strs[0];
        } else if (strs.length == 2) {
          objname = strs[0];
          attrname = strs[1];
        }
        if (argAttr.getConstraint().getDefaultValue() != null) {
          dfltVal = argAttr.getConstraint().getDefaultValue().toString();
        }
        owner.addLabelledOption("unique", "object", objname);
        owner.addLabelledOption("unique", "attribute", attrname);
        owner.addLabelledOption("unique", "type", argAttr.getType().getName());
        owner.addLabelledOption("unique", "value", dfltVal);
      }
      // TODO: “#”符号表示的过滤条件需要标注到对象上
    }
  }
}
