package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.AssignmentDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import com.doublegsoft.jcommons.utils.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import io.doublegsoft.usebase.TestBase;
import io.doublegsoft.usebase.Usebase;
import io.doublegsoft.usebase.ir.AssociationBuilder;
import io.doublegsoft.usebase.association.AssociationChain;
import io.doublegsoft.usebase.projection.ProjectionBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkflowManagementTest extends TestBase {

  private static final String OUTPUT = "out/usebase/wfm.modelbase";

  public static final String OUTPUT_DIR = "out/java/wfm/src/main/java/biz/doublegsoft/wfm/service";

  public static final String OUTPUT_ROOT = "out/java/wfm/src/main/java/biz/doublegsoft/wfm";

  @BeforeClass
  public static void initialize() throws Exception {
    new FileOutputStream(OUTPUT).close();
  }

  @Before
  public void test_gen_infos() throws Exception {
    ModelDefinition dataModel = loadModel("business/crm", "business/sms");
    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);

    List<ObjectDefinition> infos = new ArrayList<>();
    for (ObjectDefinition obj : dataModel.getObjects()) {
      ObjectDefinition rowObj = projBuilder.build(obj);
      infos.add(rowObj);
    }
    printModelbaseExtensionByUsecase(OUTPUT, null, dataModel, infos.toArray(new ObjectDefinition[0]));
  }

  /**
   * 如果想通过usebase定义来个提供modelbase已经提供了的查询工作流定义列表的API，可能不行，
   * 因为usebase的参数在查询中是准确匹配的参数，无法做到模糊查询，所以不建议。
   */
  @Test
  public void test_find_workflow_definitions() throws Exception {
    ModelDefinition dataModel = loadModel("business/wfm");
    String expr =
        "@find_workflow_definitions({workflow_definition: name}):[{workflow_definition}]";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("wfm");
    checkOriginalIndexAndObject(usecase.getReturnedObject());

    AssociationBuilder assocBuilder = new AssociationBuilder(dataModel);
    AssociationChain assocChain = assocBuilder.build(usecase.getParameterizedObject(), usecase.getReturnedObject());
    printModelbaseExtensionByUsecase(OUTPUT, usecase, dataModel);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

  /**
   * 实例化一个工作流，通常用于启动工作流。
   */
  @Test
  public void test_instantiate_workflow() throws Exception {
    ModelDefinition dataModel = loadModel("business/wfm");
    // 如何确定开始节点？这是个难点
    String expr = loadUsebaseExpression("business/wfm/instantiate_workflow.usebase");
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("wfm");
    checkOriginalIndexAndObject(usecase.getReturnedObject());

    ObjectDefinition paramObj = usecase.getParameterizedObject();
    Assert.assertEquals("workflow_instance", paramObj.getLabelledOptions("original").get("object"));

    ObjectDefinition ret = usecase.getReturnedObject();
    Assert.assertEquals("workflow_instance", ret.getAttributes()[0].getLabelledOptions("original").get("object"));
    Assert.assertEquals("workflow_instance_id", ret.getAttributes()[0].getName());
    Assert.assertEquals(10, usecase.getStatements().size());

    // 通过statement序号验证
    AssignmentDefinition assign = (AssignmentDefinition) usecase.getStatements().get(0);
    Assert.assertEquals("被赋值的变量名为wfdef", "wfdef", assign.getAssignee().getName());
    ObjectDefinition objValue = assign.getValue().getObjectValue();
    Assert.assertEquals("被赋值的变量的实际对象是workflow_definition", "workflow_definition",
        objValue.getLabelledOption("original", "object"));

    assign = (AssignmentDefinition) usecase.getStatements().get(3);
    Assert.assertEquals("被赋值的变量名为wfinst", "wfinst", assign.getAssignee().getName());
    ObjectDefinition wfinstObj = assign.getValue().getObjectValue();
    Assert.assertEquals("被赋值的变量的实际对象是workflow_instance", "workflow_instance",
        wfinstObj.getLabelledOption("original", "object"));
    Assert.assertEquals("被赋值的变量的实际数据来源来自wfdef", "wfdef", wfinstObj.getLabelledOptions("original").get("source"));

    // assign.value.arrayValue.attributes[0].value = wfactInsts.id
    assign = (AssignmentDefinition) usecase.getStatements().get(5);
    Assert.assertEquals("被赋值的变量名为wfactconninsts", "wfactconninsts", assign.getAssignee().getName());
    ObjectDefinition wfactconninstsObj = assign.getValue().getArrayValue();
    Assert.assertEquals("被赋值的变量的实际对象是workflow_action_connection_instance", "workflow_action_connection_instance",
        wfactconninstsObj.getLabelledOption("original", "object"));
    Assert.assertEquals("被赋值的变量的实际数据来源来自wfactconns", "wfactconns", wfactconninstsObj.getLabelledOptions("original").get("source"));

    assign = (AssignmentDefinition) usecase.getStatements().get(1);
    Assert.assertEquals("wfactconns", assign.getAssignee().getName());
    objValue = assign.getValue().getArrayValue();
    Assert.assertEquals("workflow_action_connection",
        objValue.getLabelledOption("original", "object"));

    assign = (AssignmentDefinition) usecase.getStatements().get(2);
    Assert.assertEquals("wfacts", assign.getAssignee().getName());
    objValue = assign.getValue().getArrayValue();
    Assert.assertEquals("workflow_action",
        objValue.getLabelledOption("original", "object"));

    assign = (AssignmentDefinition) usecase.getStatements().get(2);
    Assert.assertEquals("wfacts", assign.getAssignee().getName());
    objValue = assign.getValue().getArrayValue();
    Assert.assertEquals("workflow_action", objValue.getLabelledOption("original", "object"));

    // 通过模型查找对象验证
    ObjectDefinition wfdefObj = usecase.getContextModel().findObjectByName("#wfdef");
    Assert.assertNotNull(wfdefObj);

    ObjectDefinition wfdefArgsObj = usecase.getContextModel().findObjectByName("$wfdef");
    Assert.assertNotNull(wfdefArgsObj);

    // E2E测试
//    generateAndRun(dataModel, usecase);
//    String res = postData("/wfm/workflow_instance/find", "{" +
//        "\"workflowDefinitionId\":3" +
//        "}");
//    // 1. 能够查询到这个用户实例
//    Assert.assertFalse(Strings.isEmpty(res));
//    System.out.println(res);
//    killServer();
  }

  /**
   * 在当前节点的通过操作。
   */
  @Test
  public void test_step_workflow() throws Exception {
    ModelDefinition dataModel = loadModel("business/wfm");
    String expr = loadUsebaseExpression("business/wfm/step_workflow.usebase");
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("wfm");
    checkOriginalIndexAndObject(usecase.getReturnedObject());

    ObjectDefinition obj = usecase.getParameterizedObject();
    Assert.assertEquals("workflow_action_instance", obj.getLabelledOptions("original").get("object"));

    ObjectDefinition ret = usecase.getReturnedObject();
    Assert.assertEquals("workflow_instance", ret.getAttributes()[0].getLabelledOptions("original").get("object"));
    Assert.assertEquals("workflow_instance_id", ret.getAttributes()[0].getName());

    expr = loadUsebaseExpression("business/wfm/instantiate_workflow.usebase");
    UsecaseDefinition instantiateWorkflow = new Usebase(dataModel).parse(expr).get(0);

//    generateAndRun(dataModel, instantiateWorkflow, usecase);
//    String res = postData("/wfm/workflow_instance/find", "{" +
//        "\"workflowDefinitionId\":3" +
//        "}");
//    Map<String,Object> resp = new Gson().fromJson(res, Map.class);
//    Map<String,Object> row = ((List<Map<String,Object>>)resp.get("data")).get(0);
//    res = postData("/wfm/step_workflow", "{" +
//        "\"workflowInstanceId\":" + row.get("workflowInstanceId") +
//        "}");
//    killServer();
//    Assert.assertFalse(Strings.isEmpty(res));
//    resp = new Gson().fromJson(res, Map.class);
//    Assert.assertEquals(200, ((Number)resp.get("code")).intValue());
//    System.out.println(res);
  }

  /**
   * 在当前节点的拒绝操作。
   */
  @Test
  public void test_reject() throws Exception {
    ModelDefinition dataModel = loadModel("business/wfm");
    String expr =
        "@reject({workflow_action_instance: id!}):{workflow_instance: id} \n" +
        "|&| wf_act_curr_inst = {workflow_action_instance}#(id = id) \n" +
        "|&| wf_inst = {workflow_instance}#(id = wf_act_curr_inst.workflow_instance) \n" +
        "|&| wf_act_prev_conns = [workflow_action_connection]#(workflow_definition = wf_inst.workflow_definition, current_action = wf_act_curr_inst.id) \n" +
        "|=| {workflow_action_instance: status = 'REJECTED'}#(id = wf_act_curr_inst.id) \n" +
        "// 所有的前置节点变成PENDING状态 \n" +
        "|*| wf_act_prev_conn in wf_act_prev_conns \n" +
        "|*|=| {workflow_action_instance: status = 'PENDING'}#(id = wf_act_prev_conn.previous_action) \n" +
        "// 更新工作流实例的状态，为当前工作流节点的状态 \n" +
        "|=| {workflow_instance: status = wf_act_curr_inst.status}#(id = wf_act_curr_inst.workflow_instance) \n" +
        "// 记录工作流日志 \n" +
        "|+| {workflow_action_journal: previous_action = workflow_action_instance, status=wf_act_curr_inst.status}&wf_act_curr_inst \n";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("wfm");
    checkOriginalIndexAndObject(usecase.getReturnedObject());
//    Assert.assertEquals(7, usecase.getStatements().size());

    printModelbaseExtensionByUsecase(OUTPUT, usecase, dataModel);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

  /**
   * 撤销已完成操作的节点。
   */
  @Test
  public void test_revoke() throws Exception {
    ModelDefinition dataModel = loadModel("business/wfm");
    String expr =
        "@revoke({workflow_action_instance: id!}):{workflow_instance: id} \n" +
        "|&| wf_act_curr_inst = {workflow_action_instance}#(id = id) \n" +
        "|&| wf_inst = {workflow_instance}#(id = wf_act_curr_inst.workflow_instance) \n" +
        "|&| wf_act_next_acts = [workflow_action_connection]#(workflow_definition = wf_inst.workflow_definition, previous_action = wf_act_curr_inst.id) \n" +
        "// 当前工作流节点的所有下一个节点，更新为挂起状态 \n" +
        "|*| wf_act_next in wf_act_next_acts" +
        "|*|=| {workflow_action_instance: status = 'PENDING'}#(workflow_action = wf_act_next.next_action) \n" +
        "// TODO: 更新工作流实例的状态，为当前工作流节点的前一个节点的状态 \n" +
//        "|=| {workflow_instance: status = wf_act_curr_inst.status}#(workflow_instance = wf_act_curr_inst.workflow_instance) \n" +
        "|=| {workflow_action_instance: status = 'PENDING'}#(id = wf_act_curr_inst.id) \n" +
        "|+| {workflow_action_journal: previous_action = workflow_action_instance, status=wf_act_curr_inst.status}&wf_act_curr_inst \n";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("wfm");
    checkOriginalIndexAndObject(usecase.getReturnedObject());
//    Assert.assertEquals(6, usecase.getStatements().size());

    expr = loadUsebaseExpression("business/wfm/instantiate_workflow.usebase");
    UsecaseDefinition instantiateWorkflow = new Usebase(dataModel).parse(expr).get(0);

//    generateAndRun(dataModel, usecase, instantiateWorkflow);
//    String res = postData("/wfm/workflow_instance/find", "{" +
//        "\"workflowDefinitionId\":3" +
//        "}");
//    // 1. 能够查询到这个用户实例
//    Assert.assertFalse(Strings.isEmpty(res));
//    System.out.println(res);
//    killServer();
  }

  private void installWfmData() throws IOException, InterruptedException {
    byte[] bytes = Files.readAllBytes(new File("src/test/resources/testjson/wfm/123((45)6)78.json").toPath());
    String content = new String(bytes, StandardCharsets.UTF_8);

    Gson gson = new GsonBuilder()
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create();
    Type type = new TypeToken<Map<String, Object>>() {}.getType();
    Map<String, Object> data = gson.fromJson(content, type);

    Map<String,Object> wf = (Map<String,Object>) data.get("workflowDefinition");
    List<Map<String,Object>> wacs = (List<Map<String,Object>>) data.get("workflowActionConnections");
    List<Map<String,Object>> was = (List<Map<String,Object>>) data.get("workflowActions");
    postData("/wfm/workflow_definition/save", gson.toJson(wf));
    for (Map<String,Object> wa : was) {
      postData("/wfm/workflow_action/save", gson.toJson(wa));
    }
    for (Map<String,Object> wac : wacs) {
      postData("/wfm/workflow_action_connection/save", gson.toJson(wac));
    }
  }

  private void instantiateWorkflow() throws IOException, InterruptedException {
    postData("/wfm/instantiate_workflow", "{" +
      "\"workflowDefinitionId\":3," +
      "\"referenceId\":\"B\"," +
      "\"referenceType\":\"1\"" +
    "}");
  }

  private void generateAndRun(ModelDefinition dataModel, UsecaseDefinition... usecases) throws Exception {
    String root = "out/java/wfm";
    // 清空输出文件夹
    rm(root);
    // 用例代码生成
    for (UsecaseDefinition usecase : usecases) {
      generateCode("wfm", OUTPUT, OUTPUT_ROOT, usecase, dataModel);
    }
    // 项目代码生成并编译
    Assert.assertTrue(bash("env/java/gen-wfm.sh"));

    // 启动服务器
    new Thread(() -> {
      bash("cd out/java/wfm && java -jar target/wfm-1.0.jar");
      Assert.assertTrue(false);
    }).start();
    // 等待启动完毕
    Thread.sleep(1000 * 10);
    // 插入测试数据
    installWfmData();
    // 启动一个工作流
    instantiateWorkflow();
  }

  private void killServer() throws Exception {
    Thread.sleep(1000 * 5);
    bash("ps aux | grep \"[w]fm-1.0.jar\" | awk '{print $2}' | xargs kill -9");
  }
}
