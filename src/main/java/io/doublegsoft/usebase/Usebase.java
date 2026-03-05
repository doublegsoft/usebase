/*
** ████████████████████████████████████████████
** █▄─██─▄█─▄▄▄▄█▄─▄▄─█▄─▄─▀██▀▄─██─▄▄▄▄█▄─▄▄─█
** ██─██─██▄▄▄▄─██─▄█▀██─▄─▀██─▀─██▄▄▄▄─██─▄█▀█
** ▀▀▄▄▄▄▀▀▄▄▄▄▄▀▄▄▄▄▄▀▄▄▄▄▀▀▄▄▀▄▄▀▄▄▄▄▄▀▄▄▄▄▄▀
*/
package io.doublegsoft.usebase;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.CollectionType;
import com.doublegsoft.jcommons.metabean.type.CustomType;
import com.doublegsoft.jcommons.metabean.type.DomainType;
import com.doublegsoft.jcommons.metabean.type.PrimitiveType;
import com.doublegsoft.jcommons.metamodel.*;
import com.doublegsoft.jcommons.utils.Inflector;
import com.doublegsoft.jcommons.utils.Strings;
import io.doublegsoft.usebase.modelbase.ModelbaseHelper;
import io.doublegsoft.usebase.parser.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

import java.math.BigDecimal;
import java.util.*;

import static io.doublegsoft.usebase.parser.UsebaseParser.getOriginalText;

public class Usebase {

  private final ModelDefinition dataModel;

  private final AggregateParser aggregateParser;

  private final ArrayParser arrayParser;

  private final ObjectParser objectParser;

  private final ConditionsParser conditionsParser;

  private final ArgumentsParser argumentsParser;

  private final AttributesParser attributesParser;

  private final ValueParser valueParser;

  public Usebase(ModelDefinition dataModel) {
    this.dataModel = dataModel;
    aggregateParser = new AggregateParser(dataModel);
    arrayParser = new ArrayParser(dataModel);
    conditionsParser = new ConditionsParser(dataModel);
    attributesParser = new AttributesParser(dataModel);
    argumentsParser = new ArgumentsParser(dataModel);
    objectParser = new ObjectParser(dataModel);
    valueParser = new ValueParser(dataModel);
  }

  public List<UsecaseDefinition> parse(String expr) {
    List<UsecaseDefinition> retVal = new ArrayList<>();
    CharStream input = CharStreams.fromString(expr);
    io.doublegsoft.usebase.UsebaseLexer lexer = new io.doublegsoft.usebase.UsebaseLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    io.doublegsoft.usebase.UsebaseParser parser = new io.doublegsoft.usebase.UsebaseParser(tokens);
    io.doublegsoft.usebase.UsebaseParser.Usebase_programContext ctxProgram = parser.usebase_program();
    for (io.doublegsoft.usebase.UsebaseParser.Usebase_usecaseContext ctxUsecase : ctxProgram.usebase_usecase()) {
      retVal.add(createUsecase(ctxUsecase));
    }
    return retVal;
  }

  /**
   * Assembles {@link UsecaseDefinition} object from usebase usecase rule.
   *
   * @param ctx
   *      the usebase usecase rule
   *
   * @return {@link UsecaseDefinition} instance
   */
  private UsecaseDefinition createUsecase(io.doublegsoft.usebase.UsebaseParser.Usebase_usecaseContext ctx) {
    UsecaseDefinition retVal = new UsecaseDefinition(ctx.name.getText());
    if (ctx.usebase_arguments() != null) {
      ParameterizedObjectDefinition paramObj = new ParameterizedObjectDefinition("$" + retVal.getName(), retVal.getContextModel());
      argumentsParser.assemble(ctx.usebase_arguments(), paramObj, retVal);
      retVal.setParameterizedObject(paramObj);
      // 传入的参数需要注册成为变量
      if (!Strings.isEmpty(paramObj.getLabelledOption("original", "object"))) {
        ObjectDefinition obj = dataModel.findObjectByName(paramObj.getLabelledOption("original", "object"));
        retVal.registerVariable(obj.getName(), obj);
      }
      for (AttributeDefinition paramObjAttr : paramObj.getAttributes()) {
        retVal.registerVariable(paramObjAttr.getName(), paramObjAttr.getType());
      }
    }
    if (ctx.usebase_return() != null) {
      io.doublegsoft.usebase.UsebaseParser.Usebase_aggregateContext ctxAgg =
          ctx.usebase_return().usebase_aggregate();
      ReturnedObjectDefinition returnObj = new ReturnedObjectDefinition(":" + retVal.getName(), retVal.getContextModel());
      aggregateParser.assemble(ctxAgg, returnObj, retVal);
      if (ctxAgg.usebase_data().size() == 1 && ctxAgg.usebase_data(0).usebase_array() != null) {
        returnObj.setLabelledOption("original", "array", "true");
      }
      retVal.setReturnedObject(returnObj);
    }

    Stack<List<StatementDefinition>> stack = new Stack<>();
    stack.push(retVal.getStatements());
    List<StatementDefinition> stmts = retVal.getStatements();
    StatementDefinition prev = null;
    for (io.doublegsoft.usebase.UsebaseParser.Usebase_statementContext ctxStmt : ctx.usebase_statement()) {
      StatementDefinition stmt = createStatement(ctxStmt, retVal);
      if (prev != null) {
        if (stmt.getLevel() < prev.getLevel()) {
          int times = prev.getLevel() - stmt.getLevel();
          while (times > 0) {
            stack.pop();
            stmts = stack.peek();
            times--;
          }
        } else if (prev.isConditional() && stmt.getLevel() == prev.getLevel()){
          stack.pop();
          stmts = stack.peek();
        }
      }
      stmts.add(stmt);
      if (stmt.isConditional() || stmt.isLoop()) {
        stmts = stmt.getStatements();
        stack.push(stmts);
      }
      prev = stmt;
    }
    if (ctx.usebase_remote() != null) {
      ValueDefinition remote = new ValueDefinition();
      valueParser.assemble(ctx.usebase_remote(), remote);
      retVal.setRemote(remote);
    }

    return retVal;
  }

  /**
   * Creates {@link StatementDefinition} object and assembles its data from usebase statement rule.
   *
   * @param ctx
   *      the usebase statement rule
   *
   * @return {@link ValueDefinition} instance
   */
  private StatementDefinition createStatement(io.doublegsoft.usebase.UsebaseParser.Usebase_statementContext ctx,
                                              UsecaseDefinition usecase) {
    io.doublegsoft.usebase.UsebaseParser.Usebase_expressionContext ctxExpr = ctx.usebase_expression();
    if (ctxExpr.usebase_comparison() != null) {
      io.doublegsoft.usebase.UsebaseParser.Usebase_comparisonContext ctxComp = ctxExpr.usebase_comparison();
      ComparisonDefinition retVal = new ComparisonDefinition();
      retVal.setOperator(ctx.usebase_operator().getText());
      ValueDefinition value = new ValueDefinition();
      valueParser.assemble(ctxComp.usebase_comparison_part(0).usebase_value(), value, usecase);
      retVal.setValue(value);
      retVal.setComparand(ctxComp.usebase_comparison_part(0).comparand.getText());
      retVal.setComparator(ctxComp.usebase_comparison_part(0).usebase_comparator().getText());
      for (int i = 1; i < ctxComp.usebase_comparison_part().size(); i++) {
        io.doublegsoft.usebase.UsebaseParser.Usebase_comparison_partContext ctxPart = ctxComp.usebase_comparison_part(i);
        String conj = ctxComp.usebase_comparison_conj(i - 1).getText();
        ComparisonDefinition conjCmp = new ComparisonDefinition();
        conjCmp.setComparator(ctxPart.usebase_comparator().getText());
        conjCmp.setComparand(ctxPart.comparand.getText());
        ValueDefinition val = new ValueDefinition();
        valueParser.assemble(ctxPart.usebase_value(), val, usecase);
        conjCmp.setValue(val);
        if ("and".equals(conj)) {
          retVal.getAndComparisons().add(conjCmp);
        } else if ("or".equals(conj)){
          retVal.getOrComparisons().add(conjCmp);
        }
      }
      retVal.setOriginalText(getOriginalText(ctxComp));
      return retVal;
    } else if (ctxExpr.usebase_assignment() != null) {
      io.doublegsoft.usebase.UsebaseParser.Usebase_assignmentContext ctxAssign = ctxExpr.usebase_assignment();
      AssignmentDefinition retVal = new AssignmentDefinition();
      retVal.setOperator(ctx.usebase_operator().getText());
      String op = retVal.getOperator().substring(retVal.getOperator().length() - 2);
      if (!"&|".equals(op) && !"=|".equals(op) && !":|".equals(op) && !"#|".equals(op)) {
        throw new RuntimeException("\"" + getOriginalText(ctx) + "\" not allowed to have assignment rule");
      }
      ValueDefinition value = new ValueDefinition();
      valueParser.assemble(ctxAssign.usebase_value(), "#" + ctxAssign.variable.getText(), value, usecase);
      retVal.setValue(value);
      retVal.setAssignee(ctxAssign.variable.getText());
      retVal.setAssignOp(ctxAssign.usebase_assignop().getText());
      // 注册变量
      registerVariable(usecase, retVal.getAssignee(), value);
      if (ctx.usebase_remote() != null) {
        ValueDefinition remote = new ValueDefinition();
        valueParser.assemble(ctx.usebase_remote(), remote);
        retVal.setRemote(remote);
      }
      retVal.setOriginalText(getOriginalText(ctxAssign));
      return retVal;
    } else if (ctxExpr.item != null) {
      LoopDefinition retVal = new LoopDefinition();
      retVal.setOperator(ctx.usebase_operator().getText());
      retVal.setItemVar(ctxExpr.item.getText());
      retVal.setArrayVar(ctxExpr.array.getText());
      retVal.setOriginalText(getOriginalText(ctxExpr));
      return retVal;
    } else if (ctx.usebase_operator().getText().endsWith(".|")) {
      ReturnDefinition retVal = new ReturnDefinition();
      retVal.setOperator(ctx.usebase_operator().getText());
      if (ctxExpr.var != null) {
        retVal.addVariable(ctxExpr.var.getText());
      } else if (ctxExpr.anybase_identifier().size() > 1) {
        for (io.doublegsoft.usebase.UsebaseParser.Anybase_identifierContext ctxId : ctxExpr.anybase_identifier()) {
          retVal.addVariable(ctxId.getText());
        }
      }
      return retVal;
    } else if (ctx.usebase_operator().getText().endsWith("+|") || ctx.usebase_operator().getText().endsWith("=|")) {
      // SAVE: CREATE AND UPDATE
      SaveDefinition retVal = new SaveDefinition();
      retVal.setOperator(ctx.usebase_operator().getText());
      if (ctxExpr.usebase_object() != null) {
        io.doublegsoft.usebase.UsebaseParser.Usebase_objectContext ctxObj = ctxExpr.usebase_object();
        ObjectDefinition saveObj = new ObjectDefinition(ctxObj.name.getText(), usecase.getContextModel());
        objectParser.assemble(ctxObj, saveObj, usecase);
        if (ctxObj.alias != null) {
          retVal.setVariable(ctxObj.alias.getText());
        }
        if (retVal.getVariable() == null) {
          retVal.setVariable(saveObj.getName());
        }
        retVal.setSaveObject(saveObj);
      } else if (ctxExpr.var != null) {
        String var = ctxExpr.var.getText();
        VariableDefinition varDef = usecase.getVariable(var);
        if (varDef == null) {
          throw new IllegalArgumentException("there is no \"" + var + "\" variable in usecase context");
        }
        retVal.setVariableObject(varDef);
      } else if (ctxExpr.usebase_array() != null) {
        io.doublegsoft.usebase.UsebaseParser.Usebase_arrayContext ctxArr = ctxExpr.usebase_array();
        String saveObjName = "null";
        if (ctxArr.name != null) {
          saveObjName = ctxArr.name.getText();
        } else if (ctxArr.usebase_aggregate() != null) {
          if (ctxArr.usebase_aggregate().usebase_data().size() == 1) {
            io.doublegsoft.usebase.UsebaseParser.Usebase_dataContext ctxData = ctxArr.usebase_aggregate().usebase_data(0);
            saveObjName = ctxData.usebase_object().name.getText();
          } else {
            throw new IllegalArgumentException("为什么在此处你的聚合对象定义包含多个数据对象。");
          }
        }
        ObjectDefinition saveObj = new ObjectDefinition(saveObjName, usecase.getContextModel());
        if (ctxArr.usebase_aggregate() != null) {
          aggregateParser.assemble(ctxArr.usebase_aggregate(), saveObj, usecase);
        }
        retVal.setArray(true);
        if (retVal.getVariable() == null) {
          retVal.setVariable(Inflector.getInstance().pluralize(saveObj.getName()));
        }
        retVal.setSaveObject(saveObj);
      }
      return retVal;
    } else if (ctxExpr.usebase_invoke() != null) {
      io.doublegsoft.usebase.UsebaseParser.Usebase_invokeContext ctxInvoke = ctxExpr.usebase_invoke();
      StatementDefinition retVal = new StatementDefinition();
      retVal.setOperator(ctx.usebase_operator().getText());
      InvocationDefinition invocation = new InvocationDefinition();
      String method = ctxInvoke.anybase_identifier().getText();
      invocation.setMethod(method);
      // 方法调用的参数，简单封装
      if (ctxInvoke.usebase_arguments() != null) {
        for (io.doublegsoft.usebase.UsebaseParser.Usebase_argumentContext ctxArg : ctxInvoke.usebase_arguments().usebase_argument()) {
          if (ctxArg.anybase_identifier() != null) {
            invocation.getArguments().add(ctxArg.anybase_identifier().getText());
          }
        }
      }
      if (ctxInvoke.msg != null) {
        String msg = ctxInvoke.msg.getText();
        msg = msg.substring(1, msg.length() - 1);
        invocation.setError(msg);
      }
      retVal.setInvocation(invocation);
      retVal.setOriginalText(getOriginalText(ctxExpr));
      return retVal;
    } else {
      StatementDefinition retVal = new StatementDefinition();
      retVal.setOperator(ctx.usebase_operator().getText());
      retVal.setOriginalText(getOriginalText(ctxExpr));
      return retVal;
    }
  }

  private void registerVariable(UsecaseDefinition usecase, String name, ValueDefinition value) {
    if (value.getObjectValue() != null) {
      String origObjName = value.getObjectValue().getLabelledOption("original", "object");
      ObjectDefinition obj = dataModel.findObjectByName(origObjName);
      usecase.registerVariable(name, obj);
    } else if (value.getArrayValue() != null) {
      String origObjName = value.getArrayValue().getLabelledOption("original", "object");
      ObjectDefinition componentObj = dataModel.findObjectByName(origObjName);
      usecase.registerVariable(name, componentObj, true);
    } else if (value.getString() != null) {
      usecase.registerVariable(name, new PrimitiveType("string"));
    } else if (value.getNumber() != null) {
      usecase.registerVariable(name, new PrimitiveType("number"));
    } else if (value.getBool() != null) {
      usecase.registerVariable(name, new PrimitiveType("bool"));
    }
  }
}
