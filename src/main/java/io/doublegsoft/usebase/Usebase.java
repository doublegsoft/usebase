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
import com.doublegsoft.jcommons.metabean.type.PrimitiveType;
import com.doublegsoft.jcommons.metamodel.*;
import com.doublegsoft.jcommons.utils.Inflector;
import io.doublegsoft.usebase.modelbase.ModelbaseHelper;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

import java.math.BigDecimal;
import java.util.*;

public class Usebase {

  private final ModelDefinition dataModel;

  public Usebase(ModelDefinition dataModel) {
    this.dataModel = dataModel;
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
      for (io.doublegsoft.usebase.UsebaseParser.Usebase_argumentContext arg : ctx.usebase_arguments().usebase_argument()) {
        if (arg.usebase_aggregate() != null) {
          assembleAggregate(arg.usebase_aggregate(), paramObj, null, retVal);
        } else {
          if (ModelbaseHelper.isSystemOrExistingInObject(arg.anybase_identifier().getText(), paramObj)) {
            continue;
          }
          AttributeDefinition attr = new AttributeDefinition(arg.anybase_identifier().getText(), paramObj);
          if (arg.usebase_validation() != null) {
            attr.getConstraint().setNullable(false);
          }
          if (arg.value != null) {
            attr.getConstraint().setDefaultValue(arg.value.getText());
          }
          if (arg.usebase_validation() != null && arg.usebase_validation().required != null) {
            attr.getConstraint().setNullable(false);
          }
        }
      }
      retVal.setParameterizedObject(paramObj);
    }
    if (ctx.usebase_return() != null) {
      ReturnedObjectDefinition returnObj = new ReturnedObjectDefinition(":" + retVal.getName(), retVal.getContextModel());
      assembleAggregate(ctx.usebase_return().usebase_aggregate(), returnObj, null, retVal);
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
    retVal.setRemote(createValueFromRemote(ctx.usebase_remote()));

    return retVal;
  }

  /**
   * Assembles {@link ValueDefinition} object from usebase value rule.
   *
   * @param ctx
   *      the usebase value rule
   *
   * @param objName
   *      the new object name
   *
   * @param statement
   *      the present {@link StatementDefinition} object or null
   *
   * @param usecase
   *      the present {@link UsecaseDefinition} object, mandatory
   *
   * @return {@link ValueDefinition} instance
   */
  private ValueDefinition createValue(io.doublegsoft.usebase.UsebaseParser.Usebase_valueContext ctx,
                                      String objName,
                                      StatementDefinition statement,
                                      UsecaseDefinition usecase) {
    ValueDefinition retVal = new ValueDefinition();
    if (ctx.usebase_aggregate() != null) {
      ObjectDefinition obj = createObjectFromAggregate(ctx.usebase_aggregate(), objName, statement, usecase);
      retVal.setObjectValue(obj);
      return retVal;
    } else if (ctx.anybase_string() != null) {
      String str = ctx.anybase_string().getText();
      retVal.setString(str.substring(1, str.length() - 1));
    } else if (ctx.anybase_identifier() != null) {
      String str = ctx.anybase_identifier().getText();
      if ("now".equals(str)) {
        retVal.setKeyword(str);
      } else {
        retVal.setVariable(ctx.anybase_identifier().getText());
      }
    } else if (ctx.anybase_value() != null) {
      return createValue(ctx.anybase_value());
    }
    retVal.setOriginalText(getOriginalText(ctx));
    return retVal;
  }

  /**
   * Creates an {@link ValueDefinition} object from anybase value rule.
   *
   * @param ctx
   *      the anybase value rule
   *
   * @return {@link ValueDefinition} instance
   */
  private ValueDefinition createValue(io.doublegsoft.usebase.UsebaseParser.Anybase_valueContext ctx) {
    ValueDefinition retVal = new ValueDefinition();
    if (ctx.anybase_string() != null) {
      String str = ctx.anybase_string().getText();
      retVal.setString(str.substring(1, str.length() - 1));
    } else if (ctx.anybase_identifier() != null) {
      String str = ctx.anybase_identifier().getText();
      if ("now".equals(str)) {
        retVal.setKeyword(str);
      } else {
        retVal.setVariable(ctx.anybase_identifier().getText());
      }
    } else if (ctx.anybase_number() != null) {
      retVal.setNumber(new BigDecimal(ctx.anybase_number().getText()));
    }
    return retVal;
  }

  /**
   * Creates an {@link ObjectDefinition} instance and assembles its data from usebase object rule.
   *
   * @param ctx
   *      the usebase aggregate rule
   *
   * @param objectName
   *      the specified object name
   *
   * @param statement
   *      the present {@link StatementDefinition} object or null
   *
   * @param usecase
   *      the present {@link UsecaseDefinition} object, mandatory
   *
   * @return an {@link ObjectDefinition} instance
   *
   * @see #assembleObject(io.doublegsoft.usebase.UsebaseParser.Usebase_objectContext, ObjectDefinition, StatementDefinition, UsecaseDefinition)
   */
  private ObjectDefinition createObject(io.doublegsoft.usebase.UsebaseParser.Usebase_objectContext ctx,
                                        String objectName,
                                        StatementDefinition statement,
                                        UsecaseDefinition usecase) {
    ObjectDefinition retVal = new ObjectDefinition(objectName, usecase.getContextModel());
    assembleObject(ctx, retVal, statement, usecase);
    return retVal;
  }

  /**
   * Assembles the given {@link ObjectDefinition} instance data from usebase object rule.
   *
   * @param ctx
   *      the usebase aggregate rule
   *
   * @param obj
   *      an {@link ObjectDefinition} instance
   *
   * @param statement
   *      the present {@link StatementDefinition} object or null
   *
   * @param usecase
   *      the present {@link UsecaseDefinition} object, mandatory
   */
  private void assembleObject(io.doublegsoft.usebase.UsebaseParser.Usebase_objectContext ctx,
                              ObjectDefinition obj,
                              StatementDefinition statement,
                              UsecaseDefinition usecase) {
    if (ctx.usebase_attributes() != null) {
      for (io.doublegsoft.usebase.UsebaseParser.Usebase_attributeContext ctxAttr : ctx.usebase_attributes().usebase_attribute()) {
        AttributeDefinition attrDef = dataModel.findAttributeByNames(ctx.name.getText(), ctxAttr.name.getText());
        if (attrDef == null) {
          throw new RuntimeException("\"" + getOriginalText(ctx) + "\" has an attribute named \"" +
              ctxAttr.name.getText() + "\" not defined in data model.");
        }
        if (!ModelbaseHelper.isSystemOrExistingInObject(attrDef.getName(), obj)) {
          ModelbaseHelper.cloneAttribute(attrDef, obj);
        }
      }
    } else {
      ObjectDefinition originalObj = dataModel.findObjectByName(ctx.name.getText());
      for (AttributeDefinition attrDef : originalObj.getAttributes()) {
        if (!ModelbaseHelper.isSystemOrExistingInObject(attrDef.getName(), obj)) {
          ModelbaseHelper.cloneAttribute(attrDef, obj);
        }
      }
    }
    if (ctx.usebase_source() != null) {
      ModelbaseHelper.addOptions(obj, "original", "source",
          ctx.usebase_source().anybase_identifier().getText());
    }
    if (ctx.usebase_arguments() != null) {
      createObjectFromArguments(ctx.usebase_arguments(), obj, statement, usecase);
    }
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
      retVal.setValue(createValue(ctxComp.usebase_comparison_part(0).usebase_value(), null, retVal, usecase));
      retVal.setComparand(ctxComp.usebase_comparison_part(0).comparand.getText());
      retVal.setComparator(ctxComp.usebase_comparison_part(0).usebase_comparator().getText());
      for (int i = 1; i < ctxComp.usebase_comparison_part().size(); i++) {
        io.doublegsoft.usebase.UsebaseParser.Usebase_comparison_partContext ctxPart = ctxComp.usebase_comparison_part(i);
        String conj = ctxComp.usebase_comparison_conj(i - 1).getText();
        ComparisonDefinition conjCmp = new ComparisonDefinition();
        conjCmp.setComparator(ctxPart.usebase_comparator().getText());
        conjCmp.setComparand(ctxPart.comparand.getText());
        conjCmp.setValue(createValue(ctxPart.usebase_value(), null, retVal, usecase));
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
      if ("&|".equals(retVal.getOperator().substring(retVal.getOperator().length() - 2))) {
        // TODO: SEARCH
      } else if ("=|".equals(retVal.getOperator().substring(retVal.getOperator().length() - 2))) {
        // TODO: ASSIGNMENT
      } else if (":|".equals(retVal.getOperator().substring(retVal.getOperator().length() - 2))) {
        // TODO: UPDATE
      } else {
        throw new RuntimeException("\"" + getOriginalText(ctx) + "\" not allowed to have assignment rule");
      }
      retVal.setAssignee(ctxAssign.variable.getText());
      retVal.setAssignOp(ctxAssign.usebase_assignop().getText());
      retVal.setValue(createValue(ctxAssign.usebase_value(), "#" + ctxAssign.variable.getText(), retVal, usecase));
      retVal.setRemote(createValueFromRemote(ctx.usebase_remote()));
      retVal.setOriginalText(getOriginalText(ctxAssign));
      return retVal;
    } else if (ctxExpr.item != null) {
      LoopDefinition retVal = new LoopDefinition();
      retVal.setOperator(ctx.usebase_operator().getText());
      retVal.setItemVar(ctxExpr.item.getText());
      retVal.setArrayVar(ctxExpr.array.getText());
      retVal.setOriginalText(getOriginalText(ctxExpr));
      return retVal;
    } else if (ctx.usebase_operator().getText().endsWith("+|") || ctx.usebase_operator().getText().endsWith(":|")) {
      // SAVE: CREATE AND UPDATE
      SaveDefinition retVal = new SaveDefinition();
      retVal.setOperator(ctx.usebase_operator().getText());
      if (ctxExpr.usebase_object() != null) {
        io.doublegsoft.usebase.UsebaseParser.Usebase_objectContext ctxObj = ctxExpr.usebase_object();
        ObjectDefinition saveObj = createObject(ctxObj, "#" + ctxObj.name.getText(), retVal, usecase);
        if (ctxObj.alias != null) {
          retVal.setVariable(ctxObj.alias.getText());
        }
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

  private ObjectDefinition createObjectFromArguments(io.doublegsoft.usebase.UsebaseParser.Usebase_argumentsContext ctx,
                                                     ObjectDefinition container,
                                                     StatementDefinition statement,
                                                     UsecaseDefinition usecase) {
    String objName = "$";
    if (container.getName().startsWith("#")) {
      objName += container.getName().substring(1);
    } else {
      objName += container.getName();
    }
    if (container.getName().startsWith("$")) {
      assembleObjectFromArguments(ctx, container, statement, usecase);
      return container;
    } else {
      ObjectDefinition argsObj = new ObjectDefinition(objName, usecase.getContextModel());
      assembleObjectFromArguments(ctx, argsObj, statement, usecase);
      return argsObj;
    }
  }

  /**
   * Assembles an {@link ObjectDefinition} instance and assembles its data from usebase arguments rule.
   *
   * @param ctx
   *      the usebase aggregate rule
   *
   * @param obj
   *      the object defined in data model
   *
   * @param statement
   *      the present {@link StatementDefinition} object or null
   *
   * @param usecase
   *      the present {@link UsecaseDefinition} object, mandatory
   */
  private void assembleObjectFromArguments(io.doublegsoft.usebase.UsebaseParser.Usebase_argumentsContext ctx,
                                           ObjectDefinition obj,
                                           StatementDefinition statement,
                                           UsecaseDefinition usecase) {
    for (io.doublegsoft.usebase.UsebaseParser.Usebase_argumentContext ctxArg : ctx.usebase_argument()) {
      if (ctxArg.usebase_aggregate() != null) {
        assembleAggregate(ctxArg.usebase_aggregate(), obj, statement, usecase);
      } else if (ctxArg.anybase_identifier() != null) {
        if (ModelbaseHelper.isSystemOrExistingInObject(ctxArg.anybase_identifier().getText(), obj)) {
          continue;
        }
        AttributeDefinition propagatingAttrDef = new AttributeDefinition(ctxArg.anybase_identifier().getText(), obj);
        if (ctxArg.value != null) {
          propagatingAttrDef.getConstraint().setDefaultValue(ctxArg.value.getText());
        }
        if (ctxArg.usebase_validation() != null && ctxArg.usebase_validation().required != null) {
          propagatingAttrDef.getConstraint().setNullable(false);
        }
      }
    }
  }

  /**
   * Creates an {@link ObjectDefinition} instance with the given name and assembles its data
   * from usebase aggregate rule.
   *
   * @param ctx
   *      the usebase aggregate rule
   *
   * @param objectName
   *      the specified object name
   *
   * @param statement
   *      the present {@link StatementDefinition} object or null
   *
   * @param usecase
   *      the present {@link UsecaseDefinition} object, mandatory
   *
   * @return an {@link ObjectDefinition} instance
   *
   * @see #assembleAggregate(io.doublegsoft.usebase.UsebaseParser.Usebase_aggregateContext, ObjectDefinition, StatementDefinition, UsecaseDefinition)
   */
  private ObjectDefinition createObjectFromAggregate(io.doublegsoft.usebase.UsebaseParser.Usebase_aggregateContext ctx,
                                                     String objectName,
                                                     StatementDefinition statement,
                                                     UsecaseDefinition usecase) {
    ObjectDefinition retVal = new ObjectDefinition(objectName, usecase.getContextModel());
    assembleAggregate(ctx, retVal, statement, usecase);
    return retVal;
  }

  /**
   * Assembles fields of an {@link ObjectDefinition} instance from usebase aggregate rule.
   *
   * @param ctx
   *      the usebase aggregate rule
   *
   * @param obj
   *      an {@link ObjectDefinition} instance
   *
   * @param statement
   *      the present {@link StatementDefinition} object or null
   *
   * @param usecase
   *      the present {@link UsecaseDefinition} object, mandatory
   */
  private void assembleAggregate(io.doublegsoft.usebase.UsebaseParser.Usebase_aggregateContext ctx,
                                 ObjectDefinition obj,
                                 StatementDefinition statement,
                                 UsecaseDefinition usecase) {
    if (statement != null && (statement.getOperator().endsWith(":|") ||
        statement.getOperator().endsWith("=!"))) {
      //  更新和赋值操作，只能是简单对象，不允许合成对象
      if (ctx.usebase_data().size() != 1) {
        throw new RuntimeException("multi-objects aggregate defined in search or assignment statement not allowed");
      }
    }

    for (int i = 0; i < ctx.usebase_data().size(); i++) {
      io.doublegsoft.usebase.UsebaseParser.Usebase_conditionsContext ctxConds = null;
      if (i > 0) {
        ctxConds = ctx.usebase_conditions(i - 1);
      }
      io.doublegsoft.usebase.UsebaseParser.Usebase_dataContext ctxData = ctx.usebase_data(i);
      if (ctxData.usebase_object() != null) {
        String originalObjName = ctxData.usebase_object().name.getText();
        io.doublegsoft.usebase.UsebaseParser.Usebase_objectContext ctxObj = ctxData.usebase_object();
        ModelbaseHelper.addOptions(obj, "original", "object", originalObjName);
        if (ctxObj.usebase_attributes() != null) {
          // 显示指定（选择）了对象的属性
          for (io.doublegsoft.usebase.UsebaseParser.Usebase_attributeContext ctxAttr : ctxObj.usebase_attributes().usebase_attribute()) {
            AttributeDefinition attrDef = dataModel.findAttributeByNames(ctxObj.name.getText(), ctxAttr.name.getText());
            AttributeDefinition attrInObj = ModelbaseHelper.cloneAttribute(attrDef, obj);
            if (ctxAttr.usebase_validation() != null) {
              attrInObj.getConstraint().setNullable(false);
            }
            // 处理关联关系
            decorateConjunctionForAttribute(attrInObj, ctxConds);
          }
        } else if (ctxObj.usebase_arguments() != null) {
          for (io.doublegsoft.usebase.UsebaseParser.Usebase_argumentContext ctxArg : ctxObj.usebase_arguments().usebase_argument()) {
            AttributeDefinition attrDef = null;
            if (ctxArg.anybase_identifier() != null) {
              // (employee_name, national_id)
              attrDef = dataModel.findAttributeByNames(originalObjName, ctxArg.anybase_identifier().getText());
              if (attrDef == null) {
                attrDef = dataModel.findAttributeByNames(originalObjName, ctxArg.anybase_identifier().getText().replace(originalObjName + "_", ""));
              }
              if (attrDef != null) {
                AttributeDefinition clonedAttr = ModelbaseHelper.cloneAttribute(ctxArg.anybase_identifier().getText(), attrDef, obj);
                if (ctxArg.usebase_validation() != null) {
                  clonedAttr.getConstraint().setNullable(false);
                }
                if (ctxObj.usebase_operator_hash() != null) {
                  // 说明需要用来作为唯一性判断条件
                  obj.setLabelledOption("unique", "object", ctxObj.name.getText());
                  obj.addLabelledOption("unique", "attribute", clonedAttr.getName());
                }
              }
            } else if (ctxArg.usebase_aggregate() != null) {
              // TODO: 其他情况
            } else if (ctxArg.value != null) {
              // TODO: 其他情况
            }
          }
        } else {
          // 只有对象，为指定（选择）任何对象中的属性
          ObjectDefinition objInDataModel = dataModel.findObjectByName(ctxObj.name.getText());
          for (AttributeDefinition attrDef : objInDataModel.getAttributes()) {
            if (ModelbaseHelper.isSystemOrExistingInObject(attrDef.getName(), obj) || attrDef.getType().isCollection()) {
              continue;
            }
            AttributeDefinition attrInObj = ModelbaseHelper.cloneAttribute(attrDef, obj);
            // 处理关联关系
            decorateConjunctionForAttribute(attrInObj, ctxConds);
          }
        }
        if (ctxData.usebase_object().usebase_source() != null) {
          ModelbaseHelper.addOptions(obj, "original", "source",
              ctxData.usebase_object().usebase_source().anybase_identifier().getText());
        }
        if (ctxData.usebase_object().usebase_arguments() != null) {
          createObjectFromArguments(ctxData.usebase_object().usebase_arguments(), obj, statement, usecase);
        }
      } else if (ctxData.usebase_array() != null) {
        // 数组会额外产生内联对象
        assembleArray(ctxData.usebase_array(), obj, statement, usecase);
        AttributeDefinition attrArray = obj.getAttributes()[obj.getAttributes().length - 1];
        decorateConjunctionForAttribute(attrArray, ctxConds);
      } else if (ctxData.usebase_derivative() != null) {
        AttributeDefinition attrDeri = new AttributeDefinition(ctxData.usebase_derivative().name.getText(), obj);
        io.doublegsoft.usebase.UsebaseParser.Usebase_calculateContext ctxCalc = ctxData.usebase_derivative().usebase_calculate();
        if (ctxCalc != null) {
          if ("count".equals(ctxCalc.name.getText())) {
            attrDeri.setType(new PrimitiveType("long"));
          } else if ("sum".equals(ctxCalc.name.getText())) {
            PrimitiveType pt = new PrimitiveType("number");
            pt.setPrecision(12);
            pt.setScale(4);
            attrDeri.setType(pt);
          }
        }
        // 处理关联关系
        decorateConjunctionForAttribute(attrDeri, ctxConds);
      }
    }
  }

  /**
   * Assembles the given owner object's data from usebase array rule. And array rule is occurred on
   * below situations:
   *
   * <ul>
   *   <li>assignment value</li>
   *   <li>method argument</li>
   * </ul>
   *
   * @param ctx
   *        the usebase array rule context
   *
   * @param owner
   *        the owner object
   *
   * @param statement
   *      the present {@link StatementDefinition} object or null
   *
   * @param usecase
   *      the present {@link UsecaseDefinition} object, mandatory
   */
  private void assembleArray(io.doublegsoft.usebase.UsebaseParser.Usebase_arrayContext ctx,
                             ObjectDefinition owner,
                             StatementDefinition statement,
                             UsecaseDefinition usecase) {
    String attrName = null;
    CollectionType attrType = new CollectionType(getOriginalText(ctx));
    if (ctx.alias != null) {
      attrName = ctx.alias.getText();
    }
    if (ctx.usebase_aggregate() != null) {
      if (owner instanceof ReturnedObjectDefinition) {
        assembleAggregate(ctx.usebase_aggregate(), owner, statement, usecase);
      } else {
        io.doublegsoft.usebase.UsebaseParser.Usebase_aggregateContext ctxAgg = ctx.usebase_aggregate();
        if (!ctxAgg.usebase_data().isEmpty()) {
          if (ctxAgg.usebase_data().size() == 1) {
            io.doublegsoft.usebase.UsebaseParser.Usebase_objectContext ctxObj = ctxAgg.usebase_data().get(0).usebase_object();
            if (ctxObj == null) {
              throw new RuntimeException("usebase_object not found in array rule");
            }
            String aggObjName = ctxAgg.usebase_data().get(0).usebase_object().name.getText();
            ObjectDefinition originalObjDef = dataModel.findObjectByName(aggObjName);
            ObjectDefinition propagatedObjDef = new ObjectDefinition(aggObjName, usecase.getContextModel());
            if (ctxObj.usebase_attributes() != null) {
              for (io.doublegsoft.usebase.UsebaseParser.Usebase_attributeContext ctxAttr : ctxObj.usebase_attributes().usebase_attribute()) {
                AttributeDefinition originalAttrDef = dataModel.findAttributeByNames(aggObjName, ctxAttr.name.getText());
                if (!ModelbaseHelper.isSystemOrExistingInObject(originalObjDef.getName(), propagatedObjDef)) {
                  ModelbaseHelper.cloneAttribute(originalAttrDef, propagatedObjDef);
                }
              }
            } else {
              if (!ModelbaseHelper.isSystemOrExistingInObject(originalObjDef.getName(), propagatedObjDef)) {
                ModelbaseHelper.cloneAttributes(Arrays.asList(originalObjDef.getAttributes()), propagatedObjDef);
              }
            }
            if (ctx.usebase_source() != null) {
              ModelbaseHelper.addOptions(owner, "original", "source",
                  ctx.usebase_source().anybase_identifier().getText());
            }
            if (ctx.usebase_arguments() != null) {
              createObjectFromArguments(ctx.usebase_arguments(), owner, statement, usecase);
            }
          } else {
            if (ctx.usebase_source() != null) {
              throw new RuntimeException("multi-objects aggregate with source not allowed");
            }
            ObjectDefinition aggObj = createObjectFromAggregate(ctx.usebase_aggregate(),
                "[]" + owner.getName(), statement, usecase);
            attrType.setComponentType(aggObj);
          }
        }
      }
    } else if (ctx.name != null) {
      ObjectDefinition objInDataModel = dataModel.findObjectByName(ctx.name.getText());
      if (objInDataModel == null) {
        throw new RuntimeException("object named \"" +ctx.name.getText() + "\" not found in data model");
      }
      if (attrName == null) {
        attrName = Inflector.getInstance().pluralize(objInDataModel.getName());
      }
      if (owner instanceof ReturnedObjectDefinition) {
        for (AttributeDefinition attrDef : objInDataModel.getAttributes()) {
          if (!ModelbaseHelper.isSystemOrExistingInObject(attrDef.getName(), owner)) {
            ModelbaseHelper.cloneAttribute(attrDef, owner);
          }
        }
      }
      attrType.setComponentType(new CustomType(objInDataModel.getName(), objInDataModel));
      if (ctx.usebase_source() != null) {
        ModelbaseHelper.addOptions(owner, "original", "source",
            ctx.usebase_source().anybase_identifier().getText());
      }
      if (ctx.usebase_arguments() != null) {
        createObjectFromArguments(ctx.usebase_arguments(), owner, statement, usecase);
      }
    }
    if (!(owner instanceof ReturnedObjectDefinition)) {
      AttributeDefinition collAttr = new AttributeDefinition(attrName, owner);
      collAttr.setType(attrType);
    }
  }

  /**
   * Creates a {@link ValueDefinition} instance and assembles its data from usebase remote rule context.
   *
   * @param ctxRemote
   *        the usebase remote rule context
   *
   * @return a {@link ValueDefinition} object
   */
  private ValueDefinition createValueFromRemote(io.doublegsoft.usebase.UsebaseParser.Usebase_remoteContext ctxRemote) {
    if (ctxRemote == null) {
      return null;
    }
    ValueDefinition retVal = new ValueDefinition();
    retVal.setLabel("remote");
    if (ctxRemote.url != null) {
      retVal.getOptions().put("url", ctxRemote.url.getText());
      if (ctxRemote.url.anybase_host() != null) {
        retVal.getOptions().put("host", ctxRemote.url.anybase_host().getText());
      }
      if (ctxRemote.url.ANYBASE_TYPE_SCHEME() != null) {
        retVal.getOptions().put("scheme", ctxRemote.url.ANYBASE_TYPE_SCHEME().getText());
      }
      if (ctxRemote.url.anybase_int() != null) {
        retVal.getOptions().put("port", ctxRemote.url.anybase_int().getText());
      }
      String path = "";
      for (io.doublegsoft.usebase.UsebaseParser.Anybase_idContext p : ctxRemote.url.anybase_id()) {
        if (!path.isEmpty()) {
          path += "/";
        }
        path += p.getText();
      }
      retVal.getOptions().put("path", path);
    }
    // TODO: URL PARAMS
    return retVal;
  }

  private String getOriginalText(ParserRuleContext ctx) {
    Interval intv = new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
    return ctx.start.getInputStream().getText(intv);
  }

  private void decorateConjunctionForAttribute(AttributeDefinition attr,
                                               io.doublegsoft.usebase.UsebaseParser.Usebase_conditionsContext ctxConds) {
    if (ctxConds == null) {
      return;
    }

    Map<String, String> original = attr.getLabelledOptions("original");
    int index = 0;
    for (io.doublegsoft.usebase.UsebaseParser.Usebase_conditionContext ctxCond : ctxConds.usebase_condition()) {
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
        conjunction.put("name", leftSide);
      }
    }
  }

  private AttributeDefinition findAttributeInDataModel(String expr) {
    String[] names = expr.split("\\.");
    if (names.length == 1) {
      return null;
    }
    return dataModel.findAttributeByNames(names[0], names[1]);
  }
}
