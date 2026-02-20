package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.AssignmentDefinition;
import com.doublegsoft.jcommons.metamodel.StatementDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.SpecBase;
import io.doublegsoft.usebase.Usebase;
import io.doublegsoft.usebase.projection.ProjectionBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileOutputStream;

public class WorkflowManagementSpec extends SpecBase {

  private static final String OUTPUT = "out/usebase/wfm.modelbase";

  @BeforeClass
  public static void initialize() throws Exception {
    new FileOutputStream(OUTPUT).close();
  }

  @Before
  public void test_gen_infos() throws Exception {
    ModelDefinition dataModel = loadModel("wfm");
    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);
  }

  /**
   * 实例化一个工作流，通常用于启动工作流。
   */
  @Test
  public void test_instantiate() throws Exception {
    ModelDefinition dataModel = loadModel("wfm");
    String expr =
        "@instantiate({workflow_definition: id}, {workflow_instance: reference_id, reference_type}):{workflow_instance: id} \n" +
            "|&| wfdef = {workflow_definition}#({workflow_definition: id}) \n" +
            "|&| wfactconns = [workflow_action_connection]#({workflow_definition: id}) \n" +
            "|&| wfacts = [workflow_action]#(wfactconns) \n" +
            "|=| wfinst = {workflow_instance: status = 'ST'}&wfdef \n" +
            "|=| wfactconninsts = [{workflow_action_connection_instance}]&wfactconns \n" +
            "|=| wfactInsts = [{workflow_action_instance}]&wfacts \n" +
            "|+| wfinst \n" +
            "|+| wfactconninsts \n" +
            "|+| wfactInsts \n" +
            "|=| {workflow_action_instance: status = 'CP'}#(workflow_action) // TODO: 更新当前的操作实例状态（未实现）\n";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
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

    assign = (AssignmentDefinition) usecase.getStatements().get(4);
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
    Assert.assertEquals("workflow_definition_id", wfdefArgsObj.getAttributes()[0].getName());
    Assert.assertEquals("workflow_definition", wfdefArgsObj.getAttributes()[0]
        .getLabelledOptions("original").get("object"));

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

    printModelbaseExtensionByUsecase(usecase);
  }

  /**
   * 在当前节点的通过操作。
   */
  @Test
  public void test_process() throws Exception {
    ModelDefinition dataModel = loadModel("wfm");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@complete({workflow_action_instance: id}):{workflow_instance: id} \n" +
        "|&| wf_act_curr_inst = {workflow_action_instance}#({workflow_action_instance: id}) \n" +
        "|&| wf_act_next_insts = [{workflow_action_instance} <id=next_action_instance> {workflow_action_connection_instance}]#(current_action_instance=wf_act_curr_inst) \n" +
        "|*| wf_act_next_inst in wf_act_next_insts" +
        "|*|&| wf_act_next_prev_insts = [{workflow_action_instance} <id=previous_action_instance> {workflow_action_connection_instance}]#(current_action_instance=wf_act_next_inst) \n" +
        "|*|*| wf_act_next_prev_inst in wf_act_next_prev_insts \n" +
        "|*|*|?| wf_act_next_prev_inst.status != 'DONE' \n" +
        "|*|*|?|=| all_done = false \n" +
        "|*|?| all_done == true \n" +
        "        // 当前工作流节点的下一个节点（前置节点全部完成），则更新为挂起状态 \n" +
        "|*|?|:| {workflow_action_instance: status = 'PENDING'}#(wf_act_next_inst.id) \n" +
        "        // 创建工作流待办 \n" +
        "|*|?|+| {workflow_action_todo: workflow_action_instance = wf_act_next_inst, workflow_instance = wf_act_next_inst.workflow_instance} \n" +
        "    // 更新工作流实例的状态，为当前工作流节点的状态 \n" +
        "|:| {workflow_instance: status = wf_act_curr_inst.status}#(wf_act_curr_inst.workflow_instance.id) \n" +
        "|:| {workflow_action_instance: status = 'COMPLETED'}#(wf_act_curr_inst.id) \n" +
        "    // 记录工作流日志 \n" +
        "|+| {workflow_action_journal: previous_action = workflow_action_instance, status = wf_act_curr_inst.status}&wf_act_curr_inst \n";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    checkOriginalIndexAndObject(usecase.getReturnedObject());

    ObjectDefinition obj = usecase.getParameterizedObject();
    Assert.assertEquals("workflow_action_instance", obj.getLabelledOptions("original").get("object"));

    ObjectDefinition ret = usecase.getReturnedObject();
    Assert.assertEquals("workflow_instance", ret.getAttributes()[0].getLabelledOptions("original").get("object"));
    Assert.assertEquals("workflow_instance_id", ret.getAttributes()[0].getName());

    // 打印代码结构
    // printStatements(usecase.getStatements());

    StatementDefinition stmt2 = usecase.getStatements().get(2);
    Assert.assertEquals(3, stmt2.getStatements().size());
    Assert.assertEquals(6, usecase.getStatements().size());

    printModelbaseExtensionByUsecase(usecase);
  }

  /**
   * 在当前节点的拒绝操作。
   */
  @Test
  public void test_reject() throws Exception {
    ModelDefinition dataModel = loadModel("wfm");
    String expr =
        "@reject({workflow_action_instance: id}):{workflow_instance: id} \n" +
            "  |&| wf_act_curr_inst = {workflow_action_instance}#({workflow_action_instance: id}) \n" +
            "  |&| wf_act_prev_insts = [{workflow_action_instance} <id=next_action_instance> {workflow_action_connection_instance}]#(current_action_instance=wf_act_curr_inst) \n" +
            "  |:| {workflow_action_instance: status = 'REJECTED'}#(wf_act_curr_inst.id) \n" +
            "  // 所有的前置节点变成PENDING状态 \n" +
            "  |*| wf_act_prev_inst in wf_act_prev_insts \n" +
            "  |*|:| {workflow_action_instance: status = 'PENDING'}#(wf_act_prev_inst.id) \n" +
            "  // 更新工作流实例的状态，为当前工作流节点的状态 \n" +
            "  |:| {workflow_instance: status = wf_act_curr_inst.status}#(wf_act_curr_inst.workflow_instance.id) \n" +
            "  // 记录工作流日志 \n" +
            "  |+| {workflow_action_journal: previous_action = workflow_action_instance, status=wf_act_curr_inst.status}&wf_act_curr_inst \n";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    checkOriginalIndexAndObject(usecase.getReturnedObject());
    Assert.assertEquals(6, usecase.getStatements().size());
  }

  /**
   * 撤销已完成操作的节点。
   */
  @Test
  public void test_revoke() throws Exception {
    ModelDefinition dataModel = loadModel("wfm");
    String expr =
        "@revoke({workflow_action_instance: id}):{workflow_instance: id} \n" +
            "  |&| wf_act_curr_inst = {workflow_action_instance}#({workflow_action_instance: id}) \n" +
            "  |&| wf_act_next_insts = [{workflow_action_instance} <id=next_action_instance> {workflow_action_connection_instance}]#(current_action_instance=wf_act_curr_inst) \n" +
            "  // 当前工作流节点的所有下一个节点，更新为挂起状态 \n" +
            "  |*| wf_act_next_inst in wf_act_next_insts" +
            "  |*|:| {workflow_action_instance: status = 'PENDING'}#(wf_act_next_inst.id) \n" +
            "  // TODO: 更新工作流实例的状态，为当前工作流节点的前一个节点的状态 \n" +
            "  |:| {workflow_instance: status = wf_act_curr_inst.status}#(wf_act_curr_inst.workflow_instance.id) \n" +
            "  |:| {workflow_action_instance: status = 'PENDING'}#(wf_act_curr_inst.id) \n" +
            "  |+| {workflow_action_journal: previous_action = workflow_action_instance, status=wf_act_curr_inst.status}&wf_act_curr_inst \n";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    checkOriginalIndexAndObject(usecase.getReturnedObject());
    Assert.assertEquals(6, usecase.getStatements().size());
  }

}
