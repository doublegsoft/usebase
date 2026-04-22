package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.utils.Strings;
import io.doublegsoft.usebase.modelbase.ModelbaseHelper;

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
    List<AttributeDefinition> previousGroupingAttributes = new ArrayList<>();
    List<AttributeDefinition> currentGroupingAttributes = new ArrayList<>();
    if (ctx == null) {
      String lastObjName = "";
      List<ObjectDefinition> dataObjs = new ArrayList<>();
      Set<String> addedObjs = new HashSet<>();
      for (AttributeDefinition attr : owner.getAttributes()) {
        String origObjName = attr.getLabelledOption("original", "object");
        if (!addedObjs.contains(origObjName)) {
          dataObjs.add(dataModel.findObjectByName(origObjName));
          addedObjs.add(origObjName);
        }
        lastObjName = origObjName;
      }
      Map<String, String> conjunction = new HashMap<>();
      ObjectDefinition lastObj = dataObjs.get(dataObjs.size() - 1);
      for (int i = 0; i < dataObjs.size() - 1; i++) {
        ObjectDefinition prevObj = dataObjs.get(i);
        for (AttributeDefinition lastObjAttr : lastObj.getAttributes()) {
          if (lastObjAttr.getType().isCustom() && lastObjAttr.getType().getName().equals(prevObj.getName())) {
            // 记住：之前的都是target，当前的都是source
            String targetObject = prevObj.getName();
            String targetAttribute = prevObj.getIdentifiableAttribute().getName();
            String sourceObject = lastObj.getName();
            String sourceAttribute = lastObjAttr.getName();
            conjunction.put("target_object", targetObject);
            conjunction.put("target_attribute", targetAttribute);
            conjunction.put("source_object", sourceObject);
            conjunction.put("source_attribute", sourceAttribute);
            break;
          }
        }
        if (!conjunction.isEmpty()) {
          break;
        }
        for (AttributeDefinition prevObjAttr : prevObj.getAttributes()) {
          if (prevObjAttr.getType().isCustom() && prevObjAttr.getType().getName().equals(lastObj.getName())) {
            // 记住：之前的都是target，当前的都是source
            String targetObject = prevObj.getName();
            String targetAttribute = prevObjAttr.getName();
            String sourceObject = lastObj.getName();
            String sourceAttribute = lastObj.getIdentifiableAttribute().getName();
            conjunction.put("target_object", targetObject);
            conjunction.put("target_attribute", targetAttribute);
            conjunction.put("source_object", sourceObject);
            conjunction.put("source_attribute", sourceAttribute);
            break;
          }
        }
        if (!conjunction.isEmpty()) {
          break;
        }
      }
      for (AttributeDefinition attr : owner.getAttributes()) {
        String origObjName = attr.getLabelledOption("original", "object");
        if (origObjName.equals(lastObjName)) {
          attr.setLabelledOptions("conjunction", conjunction);
        }
      }
      return;
    }
    for (AttributeDefinition attr : owner.getAttributes()) {
      int index = Integer.parseInt(attr.getLabelledOption("original", "index"));
      if (index <= conditionsIndex) {
        previousGroupingAttributes.add(attr);
      } else if (index == conditionsIndex + 1){
        currentGroupingAttributes.add(attr);
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
      String leftObjectAlias = null;
      String rightObjectAlias = null;
      AttributeDefinition leftSideAttrInDataObj = null;
      AttributeDefinition rightSideAttrInDataObj = null;
      if (rightSide != null && rightSide.contains("'")) {
        leftSideAttrInDataObj = findLeftAttributeInDataModel(leftSide, owner, conditionsIndex);
      } else if (conjObj == null) {
        //******************************************//
        // 如果<...>中的如果不是连接对象，那就必须是明确的属性 //
        //******************************************//
        AttributeDefinition rightAttrInOwner = currentGroupingAttributes.get(currentGroupingAttributes.size() - 1);
        ObjectDefinition rightObj = dataModel.findObjectByName(rightAttrInOwner.getLabelledOption("original", "object"));
        // 注意此处
        leftSideAttrInDataObj = findLeftAttributeInDataModel(leftSide, owner, conditionsIndex);
        rightSideAttrInDataObj = findRightAttributeInDataModel(rightSide, rightObj, conditionsIndex);
        //***************************************//
        // 标注所有与其同属一个数据对象的属性相同的注解 //
        //***************************************//
        for (int j = previousGroupingAttributes.size() - 1; j >= 0; j--) {
          AttributeDefinition attr = previousGroupingAttributes.get(j);
          String origObjName = attr.getLabelledOption("original", "object");
          if (origObjName.equals(leftSideAttrInDataObj.getParent().getName())) {
            leftObjectAlias = attr.getLabelledOption("alias", "object");
            break;
          }
        }
        for (AttributeDefinition attr : currentGroupingAttributes) {
          String origObjName = attr.getLabelledOption("original", "object");
          String origAttrName = attr.getLabelledOption("original", "attribute");
          if (rightSideAttrInDataObj != null && origObjName.equals(rightSideAttrInDataObj.getParent().getName())) {
            rightObjectAlias = attr.getLabelledOption("alias", "object");
            break;
          }
        }
      } else {
        for (AttributeDefinition conjAttr : conjObj.getAttributes()) {
          for (AttributeDefinition attr : previousGroupingAttributes) {
            String origObjName = attr.getLabelledOption("original", "object");
            if (conjAttr.getType().getName().equals(origObjName)) {
              leftObjectAlias = attr.getLabelledOption("alias", "object");
              leftSideAttrInDataObj = conjAttr;
              break;
            }
          }
          if (leftSideAttrInDataObj != null) {
            break;
          }
        }
        for (AttributeDefinition conjAttr : conjObj.getAttributes()) {
          for (AttributeDefinition attr : currentGroupingAttributes) {
            String origObjName = attr.getLabelledOption("original", "object");
            if (conjAttr.getType().getName().equals(origObjName)) {
              rightObjectAlias = attr.getLabelledOption("alias", "object");
              rightSideAttrInDataObj = conjAttr;
              break;
            }
          }
          if (rightSideAttrInDataObj != null) {
            break;
          }
        }
      }
      if ((leftSideAttrInDataObj == null && rightSideAttrInDataObj == null)) {
        // 说明不需要构建关联关系
        continue;
      }
      if (leftSideAttrInDataObj == null && (rightSide == null || !rightSide.contains("'"))) {
        throw new IllegalArgumentException("'" + getOriginalText(ctxCond) + "'中的左侧变量'" + leftSide + "'在数据模型中没有找到。");
      }
      if (rightSideAttrInDataObj == null && (rightSide == null || !rightSide.contains("'"))) {
        throw new IllegalArgumentException("'" + getOriginalText(ctxCond) + "'中的右侧变量'" + rightSide + "'在数据模型中没有找到。");
      }
      for (AttributeDefinition attr : currentGroupingAttributes) {
        String origObjName = attr.getLabelledOption("original", "object");
        Map<String, String> conjunction = new HashMap<>();
        if (conjObj != null) {
          conjunction.put("object", conjObj.getName());
          conjunction.put("name", conjObj.getName());
          for (AttributeDefinition conjObjAttr : conjObj.getAttributes()) {
            for (int j = previousGroupingAttributes.size() - 1; j >= 0; j--) {
              AttributeDefinition prevConjAttr = previousGroupingAttributes.get(j);
              String prevOrigObjName = prevConjAttr.getLabelledOption("original", "object");
              if (prevOrigObjName.equals(conjObjAttr.getType().getName())) {
                conjunction.put("target_object", prevOrigObjName);
                break;
              }
            }
          }
          if (i == 0) {
            attr.setLabelledOptions("conjunction", conjunction);
          } else {
            attr.setLabelledOptions("conjunction_" + i, conjunction);
          }
        }
        assemble(origObjName, conjunction, leftSideAttrInDataObj, rightSideAttrInDataObj, leftObjectAlias, rightObjectAlias, rightSide);
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
          assemble(origObjName, conjunction, leftSideAttrInDataObj, rightSideAttrInDataObj,
              leftObjectAlias, rightObjectAlias, rightSide);
        }
      }
    }
  }

  protected AttributeDefinition findLeftAttributeInDataModel(String expr, ObjectDefinition owner, int originalIndex) {
    String[] names = expr.split("\\.");
    if (names.length == 2) {
      return ModelbaseHelper.findAttributeByNames(names[0], names[1], dataModel);
    }
    ObjectDefinition conjObj = dataModel.findObjectByName(names[0]);
    if (conjObj != null) {
      return conjObj.getIdentifiableAttribute();
    }
    // 正常在数据模型中的对象中去寻找属性
    AttributeDefinition retVal = findAttributeInDataModel(owner, expr);
    if (retVal != null) {
      return retVal;
    }
    for (AttributeDefinition attrInOwner : owner.getAttributes()) {
      int index = -1;
      try {
        index = Integer.parseInt(attrInOwner.getLabelledOption("original", "index"));
      } catch (Throwable cause) {

      }
      if (index > originalIndex) {
        break;
      }
      AttributeDefinition found = ModelbaseHelper.findAttributeByNames(
          attrInOwner.getLabelledOption("original", "object"),
          expr, dataModel
      );
//      AttributeDefinition found = findAttributeInDataModel(attrInOwner, expr);
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
      return ModelbaseHelper.findAttributeByNames(names[0], names[1], dataModel);
    }
    ObjectDefinition conjObj = dataModel.findObjectByName(names[0]);
    if (conjObj != null) {
      return conjObj.getIdentifiableAttribute();
    }
    // 正常在数据模型中的对象中去寻找属性
    AttributeDefinition retVal = findAttributeInDataModel(owner, expr);
    if (retVal != null) {
      return retVal;
    }
    for (AttributeDefinition attrInOwner : owner.getAttributes()) {
      int index = Integer.valueOf(attrInOwner.getLabelledOption("original", "index"));
      if (index <= originalIndex) {
        continue;
      }
      AttributeDefinition found = ModelbaseHelper.findAttributeByNames(
          attrInOwner.getLabelledOption("original", "object"),
          expr, dataModel
      );
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private void assemble(String origObjName, Map<String, String> conjunction,
                        AttributeDefinition leftSideAttr, AttributeDefinition rightSideAttr,
                        String leftObjectAlias, String rightObjectAlias,
                        String rightSide) {
    if (leftSideAttr == null && rightSideAttr != null && rightSide != null) {
      conjunction.put("source_object", rightSideAttr.getParent().getName());
      conjunction.put("source_attribute", rightSideAttr.getName());
      if (rightObjectAlias != null) {
        conjunction.put("source_alias", rightObjectAlias);
      }
      conjunction.put("value", rightSide.substring(1, rightSide.length() - 1));
    } else if (leftSideAttr != null && rightSideAttr == null && rightSide != null) {
      conjunction.put("source_object", leftSideAttr.getParent().getName());
      conjunction.put("source_attribute", leftSideAttr.getName());
      if (leftObjectAlias != null) {
        conjunction.put("source_alias", leftObjectAlias);
      }
      conjunction.put("value", rightSide.substring(1, rightSide.length() - 1));
    } else if (origObjName.equals(leftSideAttr.getParent().getName())) {
      conjunction.put("source_object", leftSideAttr.getParent().getName());
      conjunction.put("source_attribute", leftSideAttr.getName());
      conjunction.put("target_object", rightSideAttr.getParent().getName());
      conjunction.put("target_attribute", rightSideAttr.getName());
      if (leftObjectAlias != null) {
        conjunction.put("source_alias", leftObjectAlias);
      }
      if (rightObjectAlias != null) {
        conjunction.put("target_alias", rightObjectAlias);
      }
    } else if (origObjName.equals(rightSideAttr.getParent().getName())) {
      conjunction.put("source_object", rightSideAttr.getParent().getName());
      conjunction.put("source_attribute", rightSideAttr.getName());
      conjunction.put("target_object", leftSideAttr.getParent().getName());
      conjunction.put("target_attribute", leftSideAttr.getName());
      if (leftObjectAlias != null) {
        conjunction.put("target_alias", leftObjectAlias);
      }
      if (rightObjectAlias != null) {
        conjunction.put("source_alias", rightObjectAlias);
      }
    }
  }
}
