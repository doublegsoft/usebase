package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.CalcExprDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import com.doublegsoft.jcommons.metamodel.ValueDefinition;

import java.math.BigDecimal;

public class CalcExprParser extends UsebaseParser {

  public CalcExprParser(ModelDefinition dataModel) {
    super(dataModel);
  }

  public void assemble(io.doublegsoft.usebase.UsebaseParser.Usebase_calc_exprContext ctx,
                       CalcExprDefinition calcExpr, UsecaseDefinition usecase) {
    if (ctx.operator != null) {
      CalcExprDefinition left = new CalcExprDefinition();
      assemble(ctx.left, left, usecase);
      CalcExprDefinition right = new CalcExprDefinition();
      assemble(ctx.right, right, usecase);
      calcExpr.setLeftOperand(left);
      calcExpr.setRightOperand(right);
    } else if (ctx.usebase_calc_value() != null) {
      io.doublegsoft.usebase.UsebaseParser.Usebase_calc_valueContext ctxCalcVal = ctx.usebase_calc_value();
      if (ctxCalcVal.usebase_object() != null) {
        ValueDefinition val = new ValueDefinition();
        ObjectDefinition objVal = new ObjectDefinition(getOriginalText(ctxCalcVal.usebase_object()),
            usecase.getContextModel());
        getObjectParser().assemble(ctxCalcVal.usebase_object(), objVal, usecase);
        val.setObjectValue(objVal);
        calcExpr.setValue(val);
      } else if (ctxCalcVal.anybase_value() != null) {
        io.doublegsoft.usebase.UsebaseParser.Anybase_valueContext ctxValue = ctxCalcVal.anybase_value();
        ValueDefinition val = new ValueDefinition();
        if (ctxValue.anybase_string() != null) {
          String str = ctxValue.anybase_string().getText();
          val.setString(str.substring(1, str.length() - 1));
        } else if (ctxValue.anybase_number() != null) {
          val.setNumber(new BigDecimal(ctxValue.anybase_number().getText()));
        } else if (ctxValue.anybase_identifier() != null) {
          String text = ctxCalcVal.anybase_value().getText();
          if (text.contains(".")) {
            String[] varAndAttr = text.split("\\.");
            AttributeDefinition attr = usecase.getVariable(varAndAttr[0]).getAttribute(varAndAttr[1]);
            val.setAttributeValue(attr);
          } else {
            throw new IllegalArgumentException("没有层级的值“" + text + "”，无法获取对象中具体的属性");
          }
        }
        calcExpr.setValue(val);
      } else if (ctx.usebase_calc_expr().size() == 1) {
        // FIXME: IS IT CLEAR?
        assemble(ctx.usebase_calc_expr(0), calcExpr, usecase);
      }
    }
    if (ctx.operator != null) {
      calcExpr.setOperator(ctx.operator.getText());
    }
  }
}
