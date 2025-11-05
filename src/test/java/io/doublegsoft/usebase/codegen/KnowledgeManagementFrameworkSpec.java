/*
 * ██╗░░░██╗░██████╗███████╗██████╗░░█████╗░░██████╗███████╗
 * ██║░░░██║██╔════╝██╔════╝██╔══██╗██╔══██╗██╔════╝██╔════╝
 * ██║░░░██║╚█████╗░█████╗░░██████╦╝███████║╚█████╗░█████╗░░
 * ██║░░░██║░╚═══██╗██╔══╝░░██╔══██╗██╔══██║░╚═══██╗██╔══╝░░
 * ╚██████╔╝██████╔╝███████╗██████╦╝██║░░██║██████╔╝███████╗
 * ░╚═════╝░╚═════╝░╚══════╝╚═════╝░╚═╝░░╚═╝╚═════╝░╚══════╝
 */
package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.*;
import org.junit.Assert;
import org.junit.Test;

public class KnowledgeManagementFrameworkSpec extends SpecBase {

  /**
   * 保存知识。
   */
  @Test
  public void test_save_knowledge() throws Exception {
    ModelDefinition dataModel = loadModel("kmf");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@save_knowledge({knowledge}):{knowledge: id}";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);

    ObjectDefinition objArg = usecase.getParameterizedObject();
    AttributeDefinition[] attrs = objArg.getAttributes();
    Assert.assertTrue(attrs.length > 0);
    for (AttributeDefinition attr : attrs) {
      System.out.println(attr.getName());
    }
  }

  /**
   * 保存知识条目。
   */
  @Test
  public void test_save_knowledge_entry() throws Exception {
    ModelDefinition dataModel = loadModel("kmf");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@save_knowledge_entry({knowledge_entry: knowledge!, title!}):{knowledge_entry: id}";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    ObjectDefinition objArg = usecase.getParameterizedObject();
    Assert.assertEquals("knowledge_entry",
        objArg.getLabelledOptions("original").get("object"));
    Assert.assertFalse("知识选项不能为空",
        objArg.getAttributes()[0].getConstraint().isNullable());
    Assert.assertFalse("标题不能为空",
        objArg.getAttributes()[1].getConstraint().isNullable());

    ObjectDefinition ret = usecase.getReturnedObject();
    Assert.assertEquals("knowledge_entry", ret.getAttributes()[0].getLabelledOptions("original").get("object"));
    Assert.assertEquals("id", ret.getAttributes()[0].getName());
  }
}
