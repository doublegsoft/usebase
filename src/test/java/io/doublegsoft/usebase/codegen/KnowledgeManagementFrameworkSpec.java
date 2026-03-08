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
import io.doublegsoft.usebase.projection.ProjectionBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileOutputStream;

public class KnowledgeManagementFrameworkSpec extends SpecBase {

  private static final String OUTPUT = "out/usebase/kmf.modelbase";

  public static final String OUTPUT_DIR = "out/java/usebase-env-java/src/main/java/biz/doublegsoft/" + PROJ_NAME + "/service";


  @BeforeClass
  public static void initialize() throws Exception {
    new FileOutputStream(OUTPUT).close();
  }

  @Before
  public void test_gen_infos() throws Exception {
    ModelDefinition dataModel = loadModel("kmf");
    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);

    ObjectDefinition workflowDefinition = dataModel.findObjectByName("knowledge");
    ObjectDefinition workflowDefinitionInfo = projBuilder.build(workflowDefinition);
    ObjectDefinition workflowAction = dataModel.findObjectByName("knowledge_category");
    ObjectDefinition workflowActionInfo = projBuilder.build(workflowAction);
    ObjectDefinition workflowActionConnection = dataModel.findObjectByName("knowledge_tag");
    ObjectDefinition workflowActionConnectionInfo = projBuilder.build(workflowActionConnection);
    ObjectDefinition workflowInstance = dataModel.findObjectByName("knowledge_entry");
    ObjectDefinition workflowInstanceInfo = projBuilder.build(workflowInstance);
    ObjectDefinition workflowActionInstance = dataModel.findObjectByName("knowledge_entry_type");
    ObjectDefinition workflowActionInstanceInfo = projBuilder.build(workflowActionInstance);
    ObjectDefinition workflowActionConnectionInstance = dataModel.findObjectByName("knowledge_entry_tag");
    ObjectDefinition workflowActionConnectionInstanceInfo = projBuilder.build(workflowActionConnectionInstance);

    printModelbaseExtensionByUsecase(OUTPUT, null,
        workflowDefinitionInfo, workflowActionInfo, workflowActionConnectionInfo,
        workflowInstanceInfo, workflowActionInstanceInfo, workflowActionConnectionInstanceInfo,
        workflowActionConnectionInfo);
  }

  /**
   * 保存知识。
   */
  @Test
  public void test_save_knowledge() throws Exception {
    ModelDefinition dataModel = loadModel("kmf");
    String expr =
        "@save_knowledge({knowledge}):{knowledge: id}";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);

    ObjectDefinition objArg = usecase.getParameterizedObject();
    AttributeDefinition[] attrs = objArg.getAttributes();
    Assert.assertTrue(attrs.length > 0);
    for (AttributeDefinition attr : attrs) {
      System.out.println(attr.getName());
    }

    printModelbaseExtensionByUsecase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

  /**
   * 保存知识条目。
   */
  @Test
  public void test_save_knowledge_entry() throws Exception {
    ModelDefinition dataModel = loadModel("kmf");
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
    Assert.assertEquals("knowledge_entry_id", ret.getAttributes()[0].getName());

    printModelbaseExtensionByUsecase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }
}
