package io.doublegsoft.usebase.fundamental;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
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

    ComparisonDefinition stmtIf = (ComparisonDefinition) usecase.getStatements().get(3);
    AssignmentDefinition stmtCalc = (AssignmentDefinition) stmtIf.getStatements().get(0);

    Assert.assertTrue(stmtIf.isConditional());
    Assert.assertEquals(">", stmtIf.getComparator());
    Assert.assertEquals("amount", stmtIf.getComparand().getAttribute("amount").getName());
    Assert.assertEquals("30", stmtIf.getValue().getNumber().toString());

    Assert.assertEquals("total_amount", stmtCalc.getAssignee().getName());
    Assert.assertEquals("number", stmtCalc.getAssignee().getType().getName());
    CalcExprDefinition calcExpr = stmtCalc.getValue().getCalcExpr();
    Assert.assertEquals("amount", calcExpr.getLeftOperand().getValue().getAttributeValue().getName());
    Assert.assertEquals("*", calcExpr.getOperator());
    Assert.assertEquals("5", calcExpr.getRightOperand().getValue().getNumber().toPlainString());
  }

  @Test
  public void test_if_and_calc_array() throws Exception {
    ModelDefinition dataModel = loadModel("fundamental/aggregatelike");
    String expr = loadUsebaseExpression("fundamental/aggregatelike#if_and_calc_array");
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("aggregatelike");
    System.out.println(usecase.getOriginalText());

    Assert.assertEquals(5, usecase.getStatements().size());

    LoopDefinition stmtLoop = (LoopDefinition) usecase.getStatements().get(3);
    ComparisonDefinition stmtIf = (ComparisonDefinition) stmtLoop.getStatements().get(0);
    AssignmentDefinition stmtCalc = (AssignmentDefinition) stmtIf.getStatements().get(0);

    Assert.assertTrue(stmtIf.isConditional());
    Assert.assertEquals(">", stmtIf.getComparator());
    Assert.assertEquals("amount", stmtIf.getComparand().getAttribute("amount").getName());
    Assert.assertEquals("10", stmtIf.getValue().getNumber().toString());

    Assert.assertEquals("total_amount", stmtCalc.getAssignee().getName());
    Assert.assertEquals("number", stmtCalc.getAssignee().getType().getName());
    CalcExprDefinition calcExpr = stmtCalc.getValue().getCalcExpr();
    Assert.assertEquals("amount", calcExpr.getLeftOperand().getValue().getAttributeValue().getName());
    Assert.assertEquals("*", calcExpr.getOperator());
    Assert.assertEquals("100", calcExpr.getRightOperand().getValue().getNumber().toPlainString());
  }

}
