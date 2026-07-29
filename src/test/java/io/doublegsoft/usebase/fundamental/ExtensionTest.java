package io.doublegsoft.usebase.fundamental;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.*;
import io.doublegsoft.usebase.TestBase;
import io.doublegsoft.usebase.Usebase;
import org.junit.Assert;
import org.junit.Test;

public class ExtensionTest extends TestBase {

  @Test
  public void test_only_master() throws Exception {
    ModelDefinition dataModel = loadDataModel("fundamental/extension");
    String expr = loadUsebaseExpression("fundamental/extension#only_master");
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("extension");
    System.out.println(usecase.getOriginalText());

    Assert.assertEquals(6, usecase.getStatements().size());
    Assert.assertFalse(usecase.getStatements().get(0).isExceptional());
    Assert.assertTrue(usecase.getStatements().get(1).isExceptional());

    AssignmentDefinition stmtEncrypt = (AssignmentDefinition) usecase.getStatements().get(0);
    AssignmentDefinition stmtFind = (AssignmentDefinition) usecase.getStatements().get(1);
    AssignmentDefinition stmtCopy = (AssignmentDefinition) usecase.getStatements().get(2);

    Assert.assertEquals("password", stmtEncrypt.getAssignee().getName());
    Assert.assertEquals("string", stmtEncrypt.getAssignee().getType().getName());

    Assert.assertEquals("user", stmtFind.getAssignee().getName());
    Assert.assertEquals("user", stmtFind.getAssignee().getType().getName());

    ObjectDefinition objVal = stmtCopy.getValue().getObjectValue();
    Assert.assertNotNull(objVal);
    Assert.assertEquals("|=| ret = {online_user}&user 数据源头变量是user",
        "user", objVal.getLabelledOption("original", "source"));
    Assert.assertEquals("ret", stmtCopy.getAssignee().getName());
    Assert.assertEquals("online_user", stmtCopy.getAssignee().getType().getName());

    String varname = objVal.getLabelledOption("original", "source");
    VariableDefinition varUser = usecase.getVariable(varname);
    Assert.assertNotNull(varUser);

    // 变量验证
    VariableDefinition var = usecase.getVariable("ret");
    Assert.assertEquals("返回值类型是online_user",
        "online_user", var.getType().getName());
  }

  @Test
  public void test_master_and_details() throws Exception {
    ModelDefinition dataModel = loadDataModel("fundamental/extension");
    String expr = loadUsebaseExpression("fundamental/extension#master_and_details");
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("extension");
    System.out.println(usecase.getOriginalText());
  }
}
