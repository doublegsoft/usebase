package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;

import java.util.HashMap;
import java.util.Map;

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
   * @param attr 当前正在被组装条件的属性定义
   */
  public void assemble(io.doublegsoft.usebase.UsebaseParser.Usebase_conditionsContext ctx,
                       AttributeDefinition attr) {
    if (ctx == null) {
      return;
    }
    Map<String, String> original = attr.getLabelledOptions("original");
    int index = 0;
    for (io.doublegsoft.usebase.UsebaseParser.Usebase_conditionContext ctxCond : ctx.usebase_condition()) {
      Map<String, String> conjunction = new HashMap<>();
      if (index == 0) {
        attr.setLabelledOptions("conjunction", conjunction);
      } else {
        attr.setLabelledOptions("conjunction_" + index, conjunction);
      }
      index++;
      String leftSide = ctxCond.anybase_identifier().getText();
      String rightSide = null;
      if (ctxCond.anybase_value() != null) {
        if (ctxCond.anybase_value().anybase_identifier() != null) {
          rightSide = ctxCond.anybase_value().anybase_identifier().getText();
        } else {
          rightSide = ctxCond.anybase_value().getText();
        }
      }
      AttributeDefinition leftSideAttr = findAttributeInDataModel(leftSide);
      AttributeDefinition rightSideAttr = null;
      // TODO: rightSide是常量的情况
      if (rightSide != null) {
        rightSideAttr = findAttributeInDataModel(rightSide);
      }
      if (leftSideAttr != null && rightSideAttr != null) {
        if (original.get("object").equals(leftSideAttr.getParent().getName())) {
          conjunction.put("source_object", leftSideAttr.getParent().getName());
          conjunction.put("source_attribute", leftSideAttr.getName());
          conjunction.put("target_object", rightSideAttr.getParent().getName());
          conjunction.put("target_attribute", rightSideAttr.getName());
        } else if (original.get("object").equals(rightSideAttr.getParent().getName())) {
          conjunction.put("source_object", rightSideAttr.getParent().getName());
          conjunction.put("source_attribute", rightSideAttr.getName());
          conjunction.put("target_object", leftSideAttr.getParent().getName());
          conjunction.put("target_attribute", leftSideAttr.getName());
        } else {
          throw new IllegalArgumentException("not found attribute's object in conjunction expression");
        }
      } else if (leftSide != null) {
        conjunction.put("object", leftSide);
      }
    }
  }

}
