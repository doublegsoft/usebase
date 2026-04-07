package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import com.doublegsoft.jcommons.metamodel.VariableDefinition;
import io.doublegsoft.usebase.label.CompoundCondition;
import io.doublegsoft.usebase.label.JoinCondition;
import io.doublegsoft.usebase.label.TabularArray;
import io.doublegsoft.usebase.modelbase.ModelbaseHelper;

import java.util.List;

public class SourceParser extends UsebaseParser {

  public SourceParser(ModelDefinition dataModel) {
    super(dataModel);
  }

  public void assemble(io.doublegsoft.usebase.UsebaseParser.Usebase_sourceContext ctx, UsecaseDefinition usecase, ObjectDefinition owner) {
    String varname = ctx.var.getText();
    VariableDefinition var = usecase.getVariable(varname);
    ModelbaseHelper.addOptions(owner, "original", "source", varname);
    if (var.getComponentType() != null) {
      ObjectDefinition varObj = (ObjectDefinition) var.getComponentType();
      TabularArray tabularArray = new TabularArray();
      tabularArray.setMainVariable(varname);
      tabularArray.setMainObject(varObj);
      for (int i = 0; i < ctx.usebase_source_var().size(); i++) {
        io.doublegsoft.usebase.UsebaseParser.Usebase_conditionContext ctxCond = ctx.usebase_condition(i);
        String rightVarName = ctx.usebase_source_var(i).name.getText();
        String rightAlias = null;
        if (ctx.usebase_source_var(i).alias != null) {
          rightAlias = ctx.usebase_source_var(i).alias.getText();
        }
        VariableDefinition rightVar = usecase.getVariable(rightVarName);
        if (rightAlias != null) {
          // 同时把name作为别名
          usecase.registerVariable(rightAlias, rightVar.getType(), rightVarName);
        }
        String leftExpr = ctxCond.anybase_identifier().getText();
        ObjectDefinition leftObj = findLeftObject(leftExpr, tabularArray);
        AttributeDefinition leftAttr = leftObj.getAttribute(leftExpr);

        String rightExpr = ctxCond.anybase_value().anybase_identifier().getText();
        ObjectDefinition rightObj = (ObjectDefinition) rightVar.getComponentType();
        AttributeDefinition rightAttr = rightObj.getAttribute(rightExpr);
        CompoundCondition compCond = new CompoundCondition();
        JoinCondition joinCond = new JoinCondition();
        joinCond.setLeftObject(leftObj);
        joinCond.setLeftAttribute(leftAttr);
        joinCond.setRightObject(rightObj);
        joinCond.setRightAttribute(rightAttr);
        joinCond.setRightVariable(rightVarName);
        compCond.getJoinConditions().add(joinCond);
        tabularArray.getCompoundConditions().add(compCond);
      }
      owner.setLabelledData("tabular_array", tabularArray);
    }
  }

  private ObjectDefinition findLeftObject(String leftExpr, TabularArray tabularArray) {
    List<ObjectDefinition> leftObjs = tabularArray.getObjects();
    for (ObjectDefinition leftObj : leftObjs) {
      for (AttributeDefinition attr : leftObj.getAttributes()) {
        if (attr.getName().equals(leftExpr)) {
          return leftObj;
        }
      }
    }
    return null;
  }

}
