package io.doublegsoft.usebase.fundamental;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.type.CollectionType;
import com.doublegsoft.jcommons.metamodel.*;
import com.doublegsoft.jcommons.utils.Strings;
import io.doublegsoft.usebase.TestBase;
import io.doublegsoft.usebase.Usebase;
import org.junit.Assert;
import org.junit.Test;

public class AggregateLikeTest extends TestBase {

  @Test
  public void test_if_and_calc_object() throws Exception {
    ModelDefinition dataModel = loadModel("fundamental/aggregatelike");
    String expr = loadUsebaseExpression("fundamental/aggregatelike#if_and_calc_object");
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("aggregatelike");
    System.out.println(usecase.getOriginalText());

    Assert.assertEquals(7, usecase.getStatements().size());
    Assert.assertTrue(usecase.getStatements().get(0).isExceptional());
    Assert.assertTrue(usecase.getStatements().get(1).isExceptional());
    Assert.assertTrue(usecase.getStatements().get(2).isExceptional());

    ComparisonDefinition stmtIf = (ComparisonDefinition) usecase.getStatements().get(3);
    AssignmentDefinition stmtCalc = (AssignmentDefinition) stmtIf.getStatements().get(0);

    Assert.assertTrue(stmtIf.isConditional());
    Assert.assertEquals(">", stmtIf.getComparator());
    Assert.assertEquals("amount", stmtIf.getComparand().getAttribute("amount").getName());
    Assert.assertEquals("30", stmtIf.getValue().getNumber().toString());

    Assert.assertFalse(stmtCalc.isExceptional());
    Assert.assertEquals("total_amount", stmtCalc.getAssignee().getName());
    Assert.assertEquals("number", stmtCalc.getAssignee().getType().getName());
    CalcExprDefinition calcExpr = stmtCalc.getValue().getCalcExpr();
    Assert.assertEquals("amount", calcExpr.getLeftOperand().getValue().getAttributeValue().getName());
    Assert.assertEquals("*", calcExpr.getOperator());
    Assert.assertEquals("5", calcExpr.getRightOperand().getValue().getNumber().toPlainString());

    // 变量验证
    VariableDefinition var = usecase.getVariable("first_object_inst");
    Assert.assertEquals("first_object", var.getType().getName());
    var = usecase.getVariable("second_object_inst");
    Assert.assertEquals("second_object", var.getType().getName());
    var = usecase.getVariable("third_object_inst");
    Assert.assertEquals("third_object", var.getType().getName());
    var = usecase.getVariable("total_amount");
    Assert.assertEquals("number", var.getType().getName());
  }

  @Test
  public void test_if_and_calc_array() throws Exception {
    ModelDefinition dataModel = loadModel("fundamental/aggregatelike");
    String expr = loadUsebaseExpression("fundamental/aggregatelike#if_and_calc_array");
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("aggregatelike");
    System.out.println(usecase.getOriginalText());

    Assert.assertEquals(5, usecase.getStatements().size());
    Assert.assertTrue(usecase.getStatements().get(0).isExceptional());
    Assert.assertTrue(usecase.getStatements().get(1).isExceptional());
    Assert.assertTrue(usecase.getStatements().get(2).isExceptional());

    LoopDefinition stmtLoop = (LoopDefinition) usecase.getStatements().get(3);
    ComparisonDefinition stmtIf = (ComparisonDefinition) stmtLoop.getStatements().get(0);
    AssignmentDefinition stmtCalc = (AssignmentDefinition) stmtIf.getStatements().get(0);

    Assert.assertTrue(stmtIf.isConditional());
    Assert.assertEquals(">", stmtIf.getComparator());
    Assert.assertEquals("amount", stmtIf.getComparand().getAttribute("amount").getName());
    Assert.assertEquals("10", stmtIf.getValue().getNumber().toString());

    Assert.assertFalse(stmtCalc.isExceptional());
    Assert.assertEquals("total_amount", stmtCalc.getAssignee().getName());
    Assert.assertEquals("number", stmtCalc.getAssignee().getType().getName());
    CalcExprDefinition calcExpr = stmtCalc.getValue().getCalcExpr();
    Assert.assertEquals("amount", calcExpr.getLeftOperand().getValue().getAttributeValue().getName());
    Assert.assertEquals("*", calcExpr.getOperator());
    Assert.assertEquals("100", calcExpr.getRightOperand().getValue().getNumber().toPlainString());

    // 变量验证
    VariableDefinition var = usecase.getVariable("first_object_inst");
    Assert.assertEquals("first_object", var.getType().getName());
    var = usecase.getVariable("second_object_inst");
    Assert.assertEquals("second_object", var.getType().getName());
    var = usecase.getVariable("third_object_insts");
    Assert.assertTrue(var.getType().isCollection());
    Assert.assertEquals("third_object", ((CollectionType) var.getType()).getComponentType().getName());
    var = usecase.getVariable("total_amount");
    Assert.assertEquals("number", var.getType().getName());
  }

  @Test
  public void test_if_and_invocation() throws Exception {
    ModelDefinition dataModel = loadModel("fundamental/aggregatelike");
    String expr = loadUsebaseExpression("fundamental/aggregatelike#if_and_invocation");
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("aggregatelike");
    System.out.println(usecase.getOriginalText());

    Assert.assertEquals(8, usecase.getStatements().size());
    Assert.assertTrue(usecase.getStatements().get(0).isExceptional());
    Assert.assertTrue(usecase.getStatements().get(1).isExceptional());
    Assert.assertTrue(usecase.getStatements().get(2).isExceptional());

    ComparisonDefinition stmtIf = (ComparisonDefinition) usecase.getStatements().get(4);
    AssignmentDefinition stmtInvo = (AssignmentDefinition) stmtIf.getStatements().get(0);

    Assert.assertTrue(stmtIf.isConditional());
    Assert.assertEquals(">", stmtIf.getComparator());
    Assert.assertEquals("amount", stmtIf.getComparand().getAttribute("amount").getName());
    Assert.assertEquals("30", stmtIf.getValue().getNumber().toString());

    Assert.assertFalse(stmtInvo.isExceptional());
    Assert.assertEquals("total_amount", stmtInvo.getAssignee().getName());
    InvocationDefinition invo = stmtInvo.getValue().getInvocation();
    Assert.assertNotNull(invo);

    Assert.assertEquals("同一个变量，前面已经声明并且确定类型了",
        "number", stmtInvo.getAssignee().getType().getName());

    Assert.assertEquals("get_amount_30", invo.getMethod());
    Assert.assertEquals("third_object_inst", invo.getArguments().get(0));

    // 变量验证
    VariableDefinition var = usecase.getVariable("first_object_inst");
    Assert.assertEquals("first_object", var.getType().getName());
    var = usecase.getVariable("second_object_inst");
    Assert.assertEquals("second_object", var.getType().getName());
    var = usecase.getVariable("third_object_inst");
    Assert.assertEquals("third_object", var.getType().getName());
    var = usecase.getVariable("total_amount");
    Assert.assertEquals("number", var.getType().getName());
  }

}
