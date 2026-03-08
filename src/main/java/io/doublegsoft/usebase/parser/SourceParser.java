package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import com.doublegsoft.jcommons.metamodel.VariableDefinition;
import io.doublegsoft.usebase.label.CompoundCondition;
import io.doublegsoft.usebase.label.JoinCondition;
import io.doublegsoft.usebase.label.TabularArray;
import io.doublegsoft.usebase.modelbase.ModelbaseHelper;

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
      for (int i = 1; i < ctx.anybase_id().size(); i++) {
        io.doublegsoft.usebase.UsebaseParser.Usebase_conditionContext ctxCond = ctx.usebase_condition(i - 1);
        String rightVarName = ctx.anybase_id(i).getText();
        VariableDefinition rightVar = usecase.getVariable(rightVarName);
        String leftExpr = ctxCond.anybase_identifier().getText();
        String rightExpr = ctxCond.anybase_value().anybase_identifier().getText();
        CompoundCondition compCond = new CompoundCondition();
        JoinCondition joinCond = new JoinCondition();
        joinCond.setRightObject((ObjectDefinition) rightVar.getComponentType());
        compCond.getJoinConditions().add(joinCond);
      }
    }
  }

}
