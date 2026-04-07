package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.AssignmentDefinition;
import com.doublegsoft.jcommons.metamodel.StatementDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.SpecBase;
import io.doublegsoft.usebase.Usebase;
import io.doublegsoft.usebase.association.AssociationBuilder;
import io.doublegsoft.usebase.association.AssociationChain;
import io.doublegsoft.usebase.projection.ProjectionBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class WorkflowManagementSpec extends SpecBase {

  private static final String OUTPUT = "out/usebase/wfm.modelbase";

  public static final String OUTPUT_DIR = "out/java/usebase-env-java/src/main/java/biz/doublegsoft/" + PROJ_NAME + "/service";

  @BeforeClass
  public static void initialize() throws Exception {
    new FileOutputStream(OUTPUT).close();
  }

  @Before
  public void test_gen_infos() throws Exception {
    ModelDefinition dataModel = loadModel("crm", "sms");
    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);

    List<ObjectDefinition> infos = new ArrayList<>();
    for (ObjectDefinition obj : dataModel.getObjects()) {
      ObjectDefinition rowObj = projBuilder.build(obj);
      infos.add(rowObj);
    }
    printModelbaseExtensionByUsecase(OUTPUT, null, infos.toArray(new ObjectDefinition[0]));
  }

  /**
   * 如果想通过usebase定义来个提供modelbase已经提供了的查询工作流定义列表的API，可能不行，
   * 因为usebase的参数在查询中是准确匹配的参数，无法做到模糊查询，所以不建议。
   */
  @Test
  public void test_find_workflow_definitions() throws Exception {
    ModelDefinition dataModel = loadModel("wfm");
    String expr =
        "@find_workflow_definitions({workflow_definition: name}):[{workflow_definition}]";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("wfm");
    checkOriginalIndexAndObject(usecase.getReturnedObject());

    AssociationBuilder assocBuilder = new AssociationBuilder(dataModel);
    AssociationChain assocChain = assocBuilder.build(usecase.getParameterizedObject(), usecase.getReturnedObject());
    printModelbaseExtensionByUsecase(OUTPUT, usecase);
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
  public void test_instantiate() throws Exception {
    ModelDefinition dataModel = loadModel("wfm");
    // 如何确定开始节点？这是个难点
    String expr = loadUsebaseExpression("wfm/instantiate_workflow.usebase");
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
    Assert.assertEquals("被赋值的变量名为wfdef", "wfdef", assign.getAssignee());
    ObjectDefinition objValue = assign.getValue().getObjectValue();
    Assert.assertEquals("被赋值的变量的实际对象是workflow_definition", "workflow_definition",
        objValue.getLabelledOption("original", "object"));

    assign = (AssignmentDefinition) usecase.getStatements().get(3);
    Assert.assertEquals("被赋值的变量名为wfinst", "wfinst", assign.getAssignee());
    ObjectDefinition wfinstObj = assign.getValue().getObjectValue();
    Assert.assertEquals("被赋值的变量的实际对象是workflow_instance", "workflow_instance",
        wfinstObj.getLabelledOption("original", "object"));
    Assert.assertEquals("被赋值的变量的实际数据来源来自wfdef", "wfdef", wfinstObj.getLabelledOptions("original").get("source"));

    // assign.value.arrayValue.attributes[0].value = wfactInsts.id
    assign = (AssignmentDefinition) usecase.getStatements().get(5);
    Assert.assertEquals("被赋值的变量名为wfactconninsts", "wfactconninsts", assign.getAssignee());
    ObjectDefinition wfactconninstsObj = assign.getValue().getArrayValue();
    Assert.assertEquals("被赋值的变量的实际对象是workflow_action_connection_instance", "workflow_action_connection_instance",
        wfactconninstsObj.getLabelledOption("original", "object"));
    Assert.assertEquals("被赋值的变量的实际数据来源来自wfactconns", "wfactconns", wfactconninstsObj.getLabelledOptions("original").get("source"));

    assign = (AssignmentDefinition) usecase.getStatements().get(1);
    Assert.assertEquals("wfactconns", assign.getAssignee());
    objValue = assign.getValue().getArrayValue();
    Assert.assertEquals("workflow_action_connection",
        objValue.getLabelledOption("original", "object"));

    assign = (AssignmentDefinition) usecase.getStatements().get(2);
    Assert.assertEquals("wfacts", assign.getAssignee());
    objValue = assign.getValue().getArrayValue();
    Assert.assertEquals("workflow_action",
        objValue.getLabelledOption("original", "object"));

    assign = (AssignmentDefinition) usecase.getStatements().get(2);
    Assert.assertEquals("wfacts", assign.getAssignee());
    objValue = assign.getValue().getArrayValue();
    Assert.assertEquals("workflow_action", objValue.getLabelledOption("original", "object"));

    // 通过模型查找对象验证
    ObjectDefinition wfdefObj = usecase.getContextModel().findObjectByName("#wfdef");
    Assert.assertNotNull(wfdefObj);

    ObjectDefinition wfdefArgsObj = usecase.getContextModel().findObjectByName("$wfdef");
    Assert.assertNotNull(wfdefArgsObj);
    // FIXME: 丰富下面测试用例
//    Assert.assertEquals("id", wfdefArgsObj.getAttributes()[0].getName());
//    Assert.assertEquals("workflow_definition", wfdefArgsObj.getAttributes()[0]
//        .getLabelledOptions("original").get("object"));

//    ObjectDefinition wfactconnsObj = usecase.getContextModel().findObjectByName("#wfdef");
//    Assert.assertNotNull(wfactconnsObj);
//    Assert.assertEquals("workflow_action_connections", wfactconnsObj.getAttributes()[0].getName());
//    Assert.assertEquals("workflow_action_connection", ((CollectionType) wfactconnsObj.getAttributes()[0].getType())
//        .getComponentType().getName());

//    ObjectDefinition wfactconnsArgsObj = usecase.getContextModel().findObjectByName("$wfactconns");
//    Assert.assertNotNull(wfactconnsArgsObj);
//    Assert.assertEquals("workflow_definition_id", wfactconnsArgsObj.getAttributes()[0].getName());
//    Assert.assertEquals("workflow_definition", wfactconnsArgsObj.getAttributes()[0]
//        .getLabelledOptions("original").get("object"));

    printModelbaseExtensionByUsecase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

  /**
   * 在当前节点的通过操作。
   */
  @Test
  public void test_complete() throws Exception {
    ModelDefinition dataModel = loadModel("wfm");
    String expr =
        "@complete({workflow_action_instance: id!}):{workflow_instance: id} \n" +
        "|&| wf_act_curr_inst = {workflow_action_instance}#(id = id) \n" +
        "|&| wf_inst = {workflow_instance}#(id = wf_act_curr_inst.workflow_instance) \n" +
        "|&| wf_act_next_conns = [workflow_action_connection]#(workflow_definition = wf_inst.workflow_definition, previous_action = wf_act_curr_inst.id) \n" +
        "|*| wf_act_next_conn in wf_act_next_conns \n" +
        "|*|&| wf_act_next_acts = [workflow_action]#(id = wf_act_next_conn.current_action)] \n" +
        "|*|*| wf_act_next in wf_act_next_acts \n" +
        "|*|*|?| wf_act_next_prev_inst.status != 'DONE' \n" +
        "|*|*|?|=| all_done = false \n" +
        "|*|?| all_done == true \n" +
        "        // 当前工作流节点的下一个节点（前置节点全部完成），则更新为挂起状态 \n" +
        "|*|?|=| {workflow_action_instance: status = 'PENDING'}#(id = wf_act_next_inst.id) \n" +
        "        // 创建工作流待办 \n" +
        "|*|?|+| {workflow_action_todo: workflow_action_instance = wf_act_next_inst, workflow_instance = wf_act_next_inst.workflow_instance} \n" +
        "    // 更新工作流实例的状态，为当前工作流节点的状态 \n" +
        "|=| {workflow_instance: status = wf_act_curr_inst.status}#(id = wf_act_curr_inst.workflow_instance) \n" +
        "|=| {workflow_action_instance: status = 'COMPLETED'}#(id = wf_act_curr_inst.id) \n" +
        "    // 记录工作流日志 \n" +
        "|+| {workflow_action_journal: previous_action = workflow_action_instance, status = wf_act_curr_inst.status}&wf_act_curr_inst \n";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("wfm");
    checkOriginalIndexAndObject(usecase.getReturnedObject());

    ObjectDefinition obj = usecase.getParameterizedObject();
    Assert.assertEquals("workflow_action_instance", obj.getLabelledOptions("original").get("object"));

    ObjectDefinition ret = usecase.getReturnedObject();
    Assert.assertEquals("workflow_instance", ret.getAttributes()[0].getLabelledOptions("original").get("object"));
    Assert.assertEquals("workflow_instance_id", ret.getAttributes()[0].getName());

    // 打印代码结构
    // printStatements(usecase.getStatements());

//    StatementDefinition stmt2 = usecase.getStatements().get(2);
//    Assert.assertEquals(3, stmt2.getStatements().size());
//    Assert.assertEquals(6, usecase.getStatements().size());

    printModelbaseExtensionByUsecase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

  /**
   * 在当前节点的拒绝操作。
   */
  @Test
  public void test_reject() throws Exception {
    ModelDefinition dataModel = loadModel("wfm");
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

    printModelbaseExtensionByUsecase(OUTPUT, usecase);
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
    ModelDefinition dataModel = loadModel("wfm");
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

    printModelbaseExtensionByUsecase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

}
