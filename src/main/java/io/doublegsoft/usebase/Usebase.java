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
import com.doublegsoft.jcommons.metabean.type.*;
import com.doublegsoft.jcommons.metamodel.*;
import com.doublegsoft.jcommons.utils.Inflector;
import com.doublegsoft.jcommons.utils.Strings;
import io.doublegsoft.exprbase.Exprbase;
import io.doublegsoft.usebase.parser.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.*;

import static io.doublegsoft.usebase.parser.UsebaseParser.getOriginalText;

public class Usebase {

  private static ModelDefinition dataModel;

  private final AggregateParser aggregateParser;

  private final ArrayParser arrayParser;

  private final ObjectParser objectParser;

  private final ConditionsParser conditionsParser;

  private final ArgumentsParser argumentsParser;

  private final AttributesParser attributesParser;

  private final ValueParser valueParser;

  private final InvocationParser invocationParser;

  private final Exprbase exprbase;

  public Usebase(ModelDefinition dataModel) {
    Usebase.dataModel = dataModel;
    exprbase = new Exprbase(dataModel);
    aggregateParser = new AggregateParser(dataModel);
    arrayParser = new ArrayParser(dataModel);
    conditionsParser = new ConditionsParser(dataModel);
    attributesParser = new AttributesParser(dataModel);
    argumentsParser = new ArgumentsParser(dataModel);
    objectParser = new ObjectParser(dataModel);
    valueParser = new ValueParser(dataModel);
    invocationParser = new InvocationParser(dataModel);
  }

  public List<UsecaseDefinition> parse(String expr) {
    List<UsecaseDefinition> retVal = new ArrayList<>();
    CharStream input = CharStreams.fromString(expr);
    io.doublegsoft.usebase.UsebaseLexer lexer = new io.doublegsoft.usebase.UsebaseLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    io.doublegsoft.usebase.UsebaseParser parser = new io.doublegsoft.usebase.UsebaseParser(tokens);
    io.doublegsoft.usebase.UsebaseParser.Usebase_programContext ctxProgram = parser.usebase_program();
    for (io.doublegsoft.usebase.UsebaseParser.Usebase_usecaseContext ctxUsecase : ctxProgram.usebase_usecase()) {
      UsecaseDefinition usecase = createUsecase(ctxUsecase);
      usecase.setDataModel(dataModel);
      usecase.setOriginalText(getOriginalText(ctxUsecase));
      retVal.add(usecase);
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
//      ComparisonDefinition retVal = new ComparisonDefinition();
//      retVal.setOperator(ctx.usebase_operator().getText());
//      ValueDefinition value = new ValueDefinition();

      String comparisonExpr = getOriginalText(ctxComp.exprbase_cmp_expr());
      ComparisonDefinition retVal = exprbase.parseComparison(comparisonExpr);
      retVal.setOperator(ctx.usebase_operator().getText());

      String varname = retVal.getComparand().getName();
//      valueParser.assemble(ctxComp.usebase_comparison_part(0).usebase_value(), value, usecase);
//      retVal.setValue(value);
//      String comparandExpr = ctxComp.usebase_comparison_part(0).comparand.getText();
//      retVal.setComparator(ctxComp.usebase_comparison_part(0).usebase_comparator().getText());
//      // 根据值，重新对操作书注册类型
//      if (!comparandExpr.contains(".")) {
//        registerVariable(usecase, comparandExpr, value);
//      }
//      retVal.setComparand(getVariable(usecase, comparandExpr, getOriginalText(ctxExpr.usebase_comparison())));
//      for (int i = 1; i < ctxComp.usebase_comparison_part().size(); i++) {
//        io.doublegsoft.usebase.UsebaseParser.Usebase_comparison_partContext ctxPart = ctxComp.usebase_comparison_part(i);
//        String conj = ctxComp.usebase_comparison_conj(i - 1).getText();
//        ComparisonDefinition conjCmp = new ComparisonDefinition();
//        conjCmp.setComparator(ctxPart.usebase_comparator().getText());
//        comparandExpr = ctxPart.comparand.getText();
//        if (!comparandExpr.contains(".")) {
//          registerVariable(usecase, comparandExpr, value);
//        }
//        retVal.setComparand(getVariable(usecase, comparandExpr, getOriginalText(ctxExpr.usebase_comparison())));
//        ValueDefinition val = new ValueDefinition();
//        valueParser.assemble(ctxPart.usebase_value(), val, usecase);
//        conjCmp.setValue(val);
//        if ("and".equals(conj)) {
//          retVal.getAndComparisons().add(conjCmp);
//        } else if ("or".equals(conj)){
//          retVal.getOrComparisons().add(conjCmp);
//        }
//      }
      retVal.setOriginalText(getOriginalText(ctxComp));
      if (ctxComp.msg != null) {
        String err = ctxComp.msg.getText();
        retVal.setError(err.substring(1, err.length() - 1));
      }
      return retVal;
    } else if (ctxExpr.usebase_assignment() != null) {
      io.doublegsoft.usebase.UsebaseParser.Usebase_assignmentContext ctxAssign = ctxExpr.usebase_assignment();
      String fullOperator = ctx.usebase_operator().getText();
      String op = fullOperator.substring(fullOperator.length() - 2);
      if (!"&|".equals(op) && !"=|".equals(op) &&
          !":|".equals(op) && !"#|".equals(op) &&
          !"+|".equals(op)) {
        throw new RuntimeException("\"" + getOriginalText(ctx) + "\" not allowed to have assignment rule");
      }
      AssignmentDefinition retVal = new AssignmentDefinition();
      retVal.setOperator(fullOperator);
      ValueDefinition value = new ValueDefinition();
      valueParser.assemble(ctxAssign.usebase_value(), "#" + ctxAssign.variable.getText(), value, usecase);
      VariableDefinition var = registerVariable(usecase, ctxAssign.variable.getText(), value);
      retVal.setValue(value);
      retVal.setAssignee(var);
      retVal.setAssignOp(ctxAssign.exprbase_assignop().getText());
      if ((value.getObjectValue() != null || value.getArrayValue() != null) && (var.getType().isCustom() || var.getComponentType().isCustom())) {
        retVal.setExceptional(true);
      }
      if (ctx.usebase_remote() != null) {
        ValueDefinition remote = new ValueDefinition();
        valueParser.assemble(ctx.usebase_remote(), remote);
        retVal.setRemote(remote);
      }
      retVal.setOriginalText(getOriginalText(ctxAssign));
      return retVal;
    } else if (ctxExpr.item != null) {
      String itemVarName = ctxExpr.item.getText();
      String arrayVarName = ctxExpr.array.getText();
      VariableDefinition arrayVar = usecase.getVariable(arrayVarName);
      usecase.registerVariable(itemVarName, arrayVar.getComponentType());

      LoopDefinition retVal = new LoopDefinition();
      retVal.setOperator(ctx.usebase_operator().getText());
      retVal.registerVariable(itemVarName, arrayVar.getComponentType() );
      retVal.setItemVar(usecase.getVariable(itemVarName));
      retVal.setArrayVar(arrayVar);
      retVal.setComponentType((ObjectDefinition) arrayVar.getComponentType());
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
      return createSaveDefinition(ctx.usebase_operator().getText(), ctxExpr, usecase);
    } else if (ctxExpr.usebase_invoke() != null) {
      io.doublegsoft.usebase.UsebaseParser.Usebase_invokeContext ctxInvoke = ctxExpr.usebase_invoke();
      StatementDefinition retVal = new StatementDefinition();
      retVal.setOperator(ctx.usebase_operator().getText());
      InvocationDefinition invocation = new InvocationDefinition();
      invocationParser.assemble(ctxExpr.usebase_invoke(), invocation, usecase);

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

  private VariableDefinition registerVariable(UsecaseDefinition usecase, String name, ValueDefinition value) {
    VariableDefinition retVal = usecase.getVariable(name);
    if (retVal != null && retVal.getType() != null) {
      return retVal;
    }
    ObjectType type = guessVariableType(value);
    usecase.registerVariable(name, type);
    if (usecase.getParameterizedObject() != null) {
      for (AttributeDefinition attr : usecase.getParameterizedObject().getAttributes()) {
        if (attr.getName().equals(name)) {
          attr.setType(usecase.getVariable(name).getType());
        }
      }
    }
    return usecase.getVariable(name);
  }

  private SaveDefinition createSaveDefinition(String op, io.doublegsoft.usebase.UsebaseParser.Usebase_expressionContext ctxExpr, UsecaseDefinition usecase) {
    SaveDefinition retVal = new SaveDefinition();
    retVal.setOperator(op);
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
      if (ctxExpr.usebase_object().usebase_operator_hash() != null) {
        argumentsParser.assembleOrCreateAndThen(ctxExpr.usebase_object().usebase_arguments(), true, saveObj, usecase);
      }
      if (ctxExpr.usebase_object().msg != null) {
        String error = ctxExpr.usebase_object().msg.getText();
        retVal.setError(error);
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
  }

  private VariableDefinition getVariable(UsecaseDefinition usecase, String expr, String originalText) {
    if (!expr.contains(".")) {
      return usecase.getVariable(expr);
    } else {
      String[] comparandParts = expr.split("\\.");
      VariableDefinition var = usecase.getVariable(comparandParts[0]);
      if (var == null) {
        throw new IllegalArgumentException(originalText +
            "\n  not found \"" + comparandParts[0] + "\" variable in this usecase.");
      }
      AttributeDefinition attr = var.getAttribute(comparandParts[1]);
      if (attr == null) {
        throw new IllegalArgumentException(originalText +
            "\n  not found \"" + comparandParts[1] + "\" attribute in \"" + comparandParts[0] +
            "\" of type \"" + var.getType().getName() + "\".");
      }
      return var;
    }
  }

  public static ObjectType guessVariableType(ValueDefinition value) {
    if (value.getObjectValue() != null) {
      String origObjName = value.getObjectValue().getLabelledOption("original", "object");
      return dataModel.findObjectByName(origObjName);
    } else if (value.getArrayValue() != null) {
      CollectionType retVal = new CollectionType("");
      String origObjName = value.getArrayValue().getLabelledOption("original", "object");
      retVal.setComponentType(dataModel.findObjectByName(origObjName));
      return retVal;
    } else if (value.getString() != null) {
      return new PrimitiveType("string");
    } else if (value.getNumber() != null) {
      return new PrimitiveType("number");
    } else if (value.getBool() != null) {
      return new PrimitiveType("bool");
    } else if (value.getCalcExpr() != null) {
      CalculationDefinition calcExpr = value.getCalcExpr();
      ObjectType retVal = null;
      if (calcExpr.getLeftOperand() != null) {
        retVal = guessVariableType(calcExpr.getLeftOperand().getValue());
      }
      if (retVal != null) {
        return retVal;
      }
      if (calcExpr.getRightOperand() != null) {
        retVal = guessVariableType(calcExpr.getRightOperand().getValue());
      }
      if (retVal != null) {
        return retVal;
      }
      if (calcExpr.getValue() != null) {
        retVal = guessVariableType(calcExpr.getValue());
      }
      return retVal;
    }
    return null;
  }

}
