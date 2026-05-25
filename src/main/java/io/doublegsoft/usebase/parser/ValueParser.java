package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.CollectionType;
import com.doublegsoft.jcommons.metamodel.*;
import io.doublegsoft.usebase.Usebase;

import java.math.BigDecimal;

public class ValueParser extends UsebaseParser {

  public ValueParser(ModelDefinition dataModel) {
    super(dataModel);
  }

  public void assemble(io.doublegsoft.usebase.UsebaseParser.Anybase_valueContext ctx,
                       ValueDefinition value, UsecaseDefinition usecase) {
    if (ctx.anybase_string() != null) {
      String str = ctx.anybase_string().getText();
      value.setString(str.substring(1, str.length() - 1));
    } else if (ctx.anybase_identifier() != null) {
      String str = ctx.anybase_identifier().getText();
      if ("now".equals(str) || "null".equals(str)) {
        value.setKeyword(str);
      } else if ("true".equals(str) || "false".equals(str)){
        value.setBool(str);
      } else {
        if (str.contains(".")) {
          String[] strs = str.split("\\.");
          VariableDefinition var = usecase.getVariable(strs[0]);
          if (var == null) {
            ObjectDefinition obj = dataModel.findObjectByName(strs[0]);
            AttributeDefinition attr = obj.getAttribute(strs[1]);
            value.setAttributeValue(attr);
          } else {
            value.setVariable(var);
          }
        } else {
          VariableDefinition var = usecase.getVariable(str);
          if (var == null) {
            var = new VariableDefinition();
            var.setName(str);
          }
          value.setVariable(var);
        }
      }
    } else if (ctx.anybase_number() != null) {
      value.setNumber(new BigDecimal(ctx.anybase_number().getText()));
    }
  }

  public void assemble(io.doublegsoft.usebase.UsebaseParser.Usebase_remoteContext ctx, ValueDefinition value) {
    if (ctx == null) {
      return;
    }
    value.setLabel("remote");
    if (ctx.url != null) {
      value.getOptions().put("url", ctx.url.getText());
      if (ctx.url.anybase_host() != null) {
        value.getOptions().put("host", ctx.url.anybase_host().getText());
      }
      if (ctx.url.ANYBASE_TYPE_SCHEME() != null) {
        value.getOptions().put("scheme", ctx.url.ANYBASE_TYPE_SCHEME().getText());
      }
      if (ctx.url.anybase_int() != null) {
        value.getOptions().put("port", ctx.url.anybase_int().getText());
      }
      String path = "";
      for (io.doublegsoft.usebase.UsebaseParser.Anybase_idContext p : ctx.url.anybase_id()) {
        if (!path.isEmpty()) {
          path += "/";
        }
        path += p.getText();
      }
      value.getOptions().put("path", path);
    }
  }

  public void assemble(io.doublegsoft.usebase.UsebaseParser.Usebase_valueContext ctx,
                       String aggObjName, ValueDefinition value, UsecaseDefinition usecase) {
    if (ctx.usebase_aggregate() != null) {
      // FIXME
      ObjectDefinition aggregate = new ObjectDefinition(aggObjName, usecase.getContextModel());
      getAggregateParser().assemble(ctx.usebase_aggregate(), aggregate, usecase);
      // 如果返回的aggregate对象只有一个属性，则把这一个属性的对象作为value对象
      if (aggregate.getLabelledOption("original", "object") != null) {
        String originalObjName = aggregate.getLabelledOption("original", "object");
        ObjectDefinition dataObj = dataModel.findObjectByName(originalObjName);
        // TODO: 是否从数据对戏那个复制属性
        if ("true".equals(aggregate.getLabelledOption("original", "array"))) {
          value.setArrayValue(aggregate);
        } else {
          value.setObjectValue(aggregate);
        }
      } else if (aggregate.getAttributes().length == 1) {
        io.doublegsoft.usebase.UsebaseParser.Usebase_arrayContext ctxArr =
            ctx.usebase_aggregate().usebase_data(0).usebase_array();
        AttributeDefinition attr = aggregate.getAttributes()[0];
        ObjectDefinition obj = null;
        if (attr.getType().isCollection()) {
          obj = (ObjectDefinition) ((CollectionType)attr.getType()).getComponentType();
          aggregate.setLabelledOption("original", "object", obj.getName());
          if (ctxArr.usebase_arguments() != null) {
            // TODO
          }
          value.setArrayValue(aggregate);
        } else {
          value.setAttributeValue(attr);
        }
      } else {
        value.setObjectValue(aggregate);
      }
      return;
    } else if (ctx.anybase_string() != null) {
      String str = ctx.anybase_string().getText();
      value.setString(str.substring(1, str.length() - 1));
    } else if (ctx.anybase_identifier() != null) {
      String str = ctx.anybase_identifier().getText();
      if ("now".equals(str)) {
        value.setKeyword(str);
      } else if ("null".equals(str)) {
        value.setKeyword(str);
      } else {
        if (str.contains(".")) {
          String[] strs = str.split("\\.");
          VariableDefinition var = usecase.getVariable(strs[0]);
          if (var == null) {
            ObjectDefinition obj = dataModel.findObjectByName(strs[0]);
            AttributeDefinition attr = obj.getAttribute(strs[1]);
            value.setAttributeValue(attr);
          } else {
            value.setVariable(var);
          }
        } else {
          VariableDefinition var = usecase.getVariable(str);
          if (var == null) {
            var = new VariableDefinition();
            var.setName(str);
          }
          value.setVariable(var);
        }
      }
    } else if (ctx.anybase_value() != null) {
      assemble(ctx.anybase_value(), value, usecase);
    } else if (ctx.usebase_invoke() != null) {
      // 作为【值】的函数调用
      InvocationDefinition inv = new InvocationDefinition();
      getInvocationParser().assemble(ctx.usebase_invoke(), inv, usecase);
      value.setInvocation(inv);
    } else if (ctx.usebase_calculate() != null) {
      // TODO: 表达式计算
      io.doublegsoft.usebase.UsebaseParser.Usebase_calculateContext ctxCalc = ctx.usebase_calculate();
      if (ctxCalc.name != null /* 聚合函数 */) {

      }
    } else if (ctx.usebase_calc_expr() != null) {
      CalcExprDefinition calcExpr = new CalcExprDefinition();
      getCalcExprParser().assemble(ctx.usebase_calc_expr(), calcExpr, usecase);
      value.setCalcExpr(calcExpr);
    }
    value.setOriginalText(getOriginalText(ctx));
  }

  public void assemble(io.doublegsoft.usebase.UsebaseParser.Usebase_valueContext ctx,
                       ValueDefinition value, UsecaseDefinition usecase) {
    assemble(ctx, null, value, usecase);
  }
}
