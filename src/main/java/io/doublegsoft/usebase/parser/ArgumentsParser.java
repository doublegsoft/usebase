package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.CollectionType;
import com.doublegsoft.jcommons.metabean.type.PrimitiveType;
import com.doublegsoft.jcommons.metamodel.StatementDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import com.doublegsoft.jcommons.utils.Strings;
import io.doublegsoft.usebase.modelbase.ModelbaseHelper;

public class ArgumentsParser extends UsebaseParser {

  protected final AggregateParser aggregateParser;

  public ArgumentsParser(ModelDefinition dataModel) {
    super(dataModel);
    aggregateParser = new AggregateParser(dataModel);
  }

  public void assemble(io.doublegsoft.usebase.UsebaseParser.Usebase_argumentsContext ctx, ObjectDefinition owner) {
    if (ctx == null) {
      return;
    }
    for (io.doublegsoft.usebase.UsebaseParser.Usebase_argumentContext ctxArg : ctx.usebase_argument()) {
      if (ctxArg.usebase_aggregate() != null) {
        aggregateParser.assemble(ctxArg.usebase_aggregate(), owner);
      } else if (ctxArg.anybase_identifier() != null) {
        if (ModelbaseHelper.isSystemOrExistingInObject(ctxArg.anybase_identifier().getText(), owner)) {
          continue;
        }
        String attrname = ctxArg.anybase_identifier().getText();
        AttributeDefinition propagatingAttrDef = null;
        for (AttributeDefinition attr : owner.getAttributes()) {
          if (attr.isLabelled("original")) {
            String origObjName = attr.getLabelledOption("original", "object");
            ObjectDefinition origObj = dataModel.findObjectByName(origObjName);
            if (origObj == null) {
              continue;
            }
            for (AttributeDefinition origObjAttr : origObj.getAttributes()) {
              if (origObjAttr.getName().equals(attrname) || attrname.equals(origObj.getName() + "_" + origObjAttr.getName())) {
                propagatingAttrDef = ModelbaseHelper.cloneAttribute(origObjAttr, owner);
                break;
              }
            }
          }
        }
        if (propagatingAttrDef == null) {
          propagatingAttrDef = new AttributeDefinition(attrname, owner);
          propagatingAttrDef.setType(new PrimitiveType("string"));
        }
        if (ctxArg.value != null) {
          String text = ctxArg.value.getText();
          if (text.startsWith("'")) {
            text = text.substring(1, text.length() - 1);
          }
          propagatingAttrDef.getConstraint().setDefaultValue(text);
        }
        if (ctxArg.usebase_validation() != null && ctxArg.usebase_validation().required != null) {
          propagatingAttrDef.getConstraint().setNullable(false);
        }
        if (ctxArg.anybase_id() != null) {
          propagatingAttrDef.setAlias(ctxArg.anybase_id().getText());
        }
      } else if (ctxArg.usebase_sysobj() != null) {
        throw new IllegalArgumentException("'" + getOriginalText(ctxArg.anybase_value()) +
            "' is not supported as an argument yet.");
      } else if (ctxArg.anybase_value() != null) {
        throw new IllegalArgumentException("'" + getOriginalText(ctxArg.anybase_value()) +
            "' is not supported as an argument yet.");
      }
    }
  }

  /**
   * 解析 usebase 参数，并根据参数中的属性表达式
   * 为当前对象定义（owner）设置 unique 相关的标注信息。
   *
   * usebase 参数支持的形式：
   *   1) obj.attr   → 指定对象的属性
   *   2) attr       → 默认当前对象的属性
   *
   * 最终效果：
   *   owner.unique.object    = 属性所属对象名
   *   owner.unique.attribute = 属性名
   *
   * @param ctx   usebase 参数上下文（ANTLR 解析树）
   * @param owner 当前正在被修饰的对象定义
   */
  public void decorate(io.doublegsoft.usebase.UsebaseParser.Usebase_argumentsContext ctx, ObjectDefinition owner) {
    if (ctx == null) {
      return;
    }
    String origObjName = owner.getLabelledOption("original", "object");
    for (io.doublegsoft.usebase.UsebaseParser.Usebase_argumentContext ctxArg : ctx.usebase_argument()) {
      if (ctxArg.anybase_identifier() != null) {
        String id = ctxArg.anybase_identifier().getText();
        String[] strs = id.split("\\.");
        if (strs.length <= 2) {
          String objName = strs[0];
          String attrName = "";
          if (strs.length == 2) {
            attrName = strs[1];
          } else {
            objName = owner.getName();
            attrName = strs[0];
          }
          if (objName.startsWith("#") || objName.startsWith("$") || objName.startsWith(":")) {
            objName = objName.substring(1);
          }
          if (!Strings.isEmpty(origObjName)) {
            objName = origObjName;
          }
          ObjectDefinition dataObj = dataModel.findObjectByName(objName);
          if (dataObj == null && owner.getAttributes().length == 1) {
            // 通常为参数的所属对象是个数组对象，而且这个数组只有一个数据对象
            AttributeDefinition attr = owner.getAttributes()[0];
            if (attr.getType().isCollection()) {
              CollectionType collType = (CollectionType) attr.getType();
              objName = collType.getComponentType().getName();
            }
          }
          AttributeDefinition attr = dataModel.findAttributeByNames(objName, attrName);
          if (attr == null) {
            String newAttrName = attrName.replace(objName + "_", "");
            attr = dataModel.findAttributeByNames(objName, newAttrName);
          }
          if (attr == null) {
            String newAttrName = attrName.replace("_id", "");
            attr = dataModel.findAttributeByNames(objName, newAttrName);
          }
          if (attr == null) {
            if (ctxArg.attr != null) {
              attr = dataModel.findAttributeByNames(objName, ctxArg.attr.getText());
            }
          }
          if (attr == null) {
            for (AttributeDefinition ownerAttr : owner.getAttributes()) {
              if (ownerAttr.getType().isCollection()) {
                CollectionType collType = (CollectionType) ownerAttr.getType();
                origObjName = collType.getComponentType().getName();
              } else {
                origObjName = ownerAttr.getLabelledOption("original", "object");
              }
              attr = dataModel.findAttributeByNames(origObjName, attrName);
              if (attr != null) {
                break;
              }
            }
          }
          if (attr == null) {
            return;
//             throw new IllegalArgumentException("'" + getOriginalText(ctxArg) + "' attribute is not found in data model");
          }
          owner.setLabelledOption("unique", "object", attr.getParent().getName());
          owner.setLabelledOption("unique", "attribute", attr.getName());
        } else {
          throw new IllegalArgumentException("'" + getOriginalText(ctxArg) + "' is not an attribute expression");
        }
      } else {
//        throw new UnsupportedOperationException("Decorating an object definition with arguments is only " +
//            "support for identifier expression. '" + getOriginalText(ctxArg) + "' is not supported.");
      }
    }
  }

}
