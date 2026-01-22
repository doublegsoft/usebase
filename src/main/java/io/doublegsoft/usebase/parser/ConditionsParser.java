package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.utils.Strings;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class ConditionsParser extends UsebaseParser {

  public ConditionsParser(ModelDefinition dataModel) {
    super(dataModel);
  }

  /**
   * 解析 usebase 条件表达式，并将条件关系组装（assemble）
   * 到属性定义（AttributeDefinition）中。
   *
   * usebase 条件示例（概念）：
   *   a.id = b.ref_id
   *   a.code = b.code
   *
   * 解析后会生成一组 conjunction（条件关系）：
   *   conjunction:
   *     source_object
   *     source_attribute
   *     target_object
   *     target_attribute
   *
   * 多个条件会按顺序存为：
   *   conjunction
   *   conjunction_1
   *   conjunction_2
   *   ...
   *
   * @param ctx  usebase 条件上下文（ANTLR 解析树）
   */
  public void assemble(io.doublegsoft.usebase.UsebaseParser.Usebase_conditionsContext ctx,
                       ObjectDefinition owner, int conditionsIndex) {
    if (ctx == null) {
      return;
    }
    List<AttributeDefinition> previousGroupingAttributes = new ArrayList<>();
    List<AttributeDefinition> presentGroupingAttributes = new ArrayList<>();
    for (AttributeDefinition attr : owner.getAttributes()) {
      int index = Integer.parseInt(attr.getLabelledOption("original", "index"));
      if (index <= conditionsIndex) {
        previousGroupingAttributes.add(attr);
      } else if (index == conditionsIndex + 1){
        presentGroupingAttributes.add(attr);
      }
    }
    for (int i = 0; i < ctx.usebase_condition().size(); i++) {
      io.doublegsoft.usebase.UsebaseParser.Usebase_conditionContext ctxCond = ctx.usebase_condition(i);
      String leftSide = ctxCond.anybase_identifier().getText();
      String rightSide = null;
      if (ctxCond.anybase_value() != null) {
        if (ctxCond.anybase_value().anybase_identifier() != null) {
          rightSide = ctxCond.anybase_value().anybase_identifier().getText();
        } else {
          rightSide = ctxCond.anybase_value().getText();
        }
      }
      // <bill_id> 或者 <user_role> 这种类型可能是属性，也有可能是对象
      ObjectDefinition conjObj = null;
      if (!leftSide.contains(".") && rightSide == null) {
        conjObj = dataModel.findObjectByName(leftSide);
        if (conjObj == null) {
          rightSide = leftSide;
        }
      }
      AttributeDefinition leftSideAttr = null;
      AttributeDefinition rightSideAttr = null;
      if (rightSide != null && rightSide.contains("'")) {
        leftSideAttr = findLeftAttributeInDataModel(leftSide, owner, conditionsIndex);
        rightSideAttr = findRightAttributeInDataModel(leftSide, owner, conditionsIndex);
      } else if (conjObj == null) {
        leftSideAttr = findLeftAttributeInDataModel(leftSide, owner, conditionsIndex);
        rightSideAttr = findRightAttributeInDataModel(rightSide, owner, conditionsIndex);
      } else {
        for (AttributeDefinition conjAttr : conjObj.getAttributes()) {
          for (AttributeDefinition attr : previousGroupingAttributes) {
            String origObjName = attr.getLabelledOption("original", "object");
            if (conjAttr.getType().getName().equals(origObjName)) {
              leftSideAttr = conjAttr;
              break;
            }
          }
          if (leftSideAttr != null) {
            break;
          }
        }
        for (AttributeDefinition conjAttr : conjObj.getAttributes()) {
          for (AttributeDefinition attr : presentGroupingAttributes) {
            String origObjName = attr.getLabelledOption("original", "object");
            if (conjAttr.getType().getName().equals(origObjName)) {
              rightSideAttr = conjAttr;
              break;
            }
          }
          if (rightSideAttr != null) {
            break;
          }
        }
      }
      if (leftSideAttr == null && rightSideAttr == null) {
        // 说明不需要构建关联关系
        continue;
      }
      if (leftSideAttr == null && (rightSide == null || !rightSide.contains("'"))) {
        throw new IllegalArgumentException("'" + getOriginalText(ctxCond) + "'中的左侧变量'" + leftSide + "'在数据模型中没有找到。");
      }
      if (rightSideAttr == null && (rightSide == null || !rightSide.contains("'"))) {
        throw new IllegalArgumentException("'" + getOriginalText(ctxCond) + "'中的右侧变量'" + rightSide + "'在数据模型中没有找到。");
      }
      for (AttributeDefinition attr : presentGroupingAttributes) {
        String origObjName = attr.getLabelledOption("original", "object");
        Map<String, String> conjunction = new HashMap<>();
        if (i == 0) {
          attr.setLabelledOptions("conjunction", conjunction);
        } else {
          attr.setLabelledOptions("conjunction_" + i, conjunction);
        }
        assemble(origObjName, conjunction, leftSideAttr, rightSideAttr, rightSide);
      }
      if (conditionsIndex == 0) {
        for (AttributeDefinition attr : previousGroupingAttributes) {
          String origObjName = attr.getLabelledOption("original", "object");
          Map<String, String> conjunction = new HashMap<>();
          if (i == 0) {
            attr.setLabelledOptions("conjunction", conjunction);
          } else {
            attr.setLabelledOptions("conjunction_" + i, conjunction);
          }
          assemble(origObjName, conjunction, leftSideAttr, rightSideAttr, rightSide);
        }
      }
    }
  }

  protected AttributeDefinition findLeftAttributeInDataModel(String expr, ObjectDefinition owner, int originalIndex) {
    String[] names = expr.split("\\.");
    if (names.length == 2) {
      return findAttributeInDataModel(names[0], names[1]);
    }
    ObjectDefinition conjObj = dataModel.findObjectByName(names[0]);
    if (conjObj != null) {
      return conjObj.getIdentifiableAttribute();
    }
    for (AttributeDefinition attrInOwner : owner.getAttributes()) {
      int index = Integer.valueOf(attrInOwner.getLabelledOption("original", "index"));
      if (index > originalIndex) {
        break;
      }
      AttributeDefinition found = findAttributeInDataModel(attrInOwner, expr);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private AttributeDefinition findRightAttributeInDataModel(String expr,
                                                            ObjectDefinition owner,
                                                            int originalIndex) {
    if (expr == null) {
      return null;
    }
    String[] names = expr.split("\\.");
    if (names.length == 2) {
      return findAttributeInDataModel(names[0], names[1]);
    }
    ObjectDefinition conjObj = dataModel.findObjectByName(names[0]);
    if (conjObj != null) {
      return conjObj.getIdentifiableAttribute();
    }
    for (AttributeDefinition attrInOwner : owner.getAttributes()) {
      int index = Integer.valueOf(attrInOwner.getLabelledOption("original", "index"));
      if (index <= originalIndex) {
        continue;
      }
      AttributeDefinition found = findAttributeInDataModel(attrInOwner, expr);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private AttributeDefinition findAttributeInDataModel(AttributeDefinition attrInOwner, String expr) {
    String origObjName = attrInOwner.getLabelledOption("original", "object");
    ObjectDefinition obj = dataModel.findObjectByName(origObjName);
    if (obj == null) {
      return null;
    }
    for (AttributeDefinition attrInObj : obj.getAttributes()) {
      String attrname = attrInObj.getName();
      if (attrInObj.getType().isCustom() && expr.equals(attrInObj.getType().getName() + "_id")) {
        return attrInObj;
      }
      if (expr.equals(attrname) || expr.equals(attrInObj.getName() + "_" + attrname)) {
        return attrInObj;
      }
    }
    return null;
  }

  private AttributeDefinition findAttributeInDataModel(String objname, String attrname) {
    ObjectDefinition obj = dataModel.findObjectByName(objname);
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

  private void assemble(String origObjName, Map<String, String> conjunction, AttributeDefinition leftSideAttr, AttributeDefinition rightSideAttr, String rightSide) {
    if (leftSideAttr == null && rightSideAttr != null && rightSide != null) {
      conjunction.put("source_object", rightSideAttr.getParent().getName());
      conjunction.put("source_attribute", rightSideAttr.getName());
      conjunction.put("value", rightSide.substring(1, rightSide.length() - 1));
    } else if (leftSideAttr != null && rightSideAttr == null && rightSide != null) {
      conjunction.put("source_object", leftSideAttr.getParent().getName());
      conjunction.put("source_attribute", leftSideAttr.getName());
      conjunction.put("value", rightSide.substring(1, rightSide.length() - 1));
    } else if (origObjName.equals(leftSideAttr.getParent().getName())) {
      conjunction.put("source_object", leftSideAttr.getParent().getName());
      conjunction.put("source_attribute", leftSideAttr.getName());
      conjunction.put("target_object", rightSideAttr.getParent().getName());
      conjunction.put("target_attribute", rightSideAttr.getName());
    } else if (origObjName.equals(rightSideAttr.getParent().getName())) {
      conjunction.put("source_object", rightSideAttr.getParent().getName());
      conjunction.put("source_attribute", rightSideAttr.getName());
      conjunction.put("target_object", leftSideAttr.getParent().getName());
      conjunction.put("target_attribute", leftSideAttr.getName());
    }
  }
}
