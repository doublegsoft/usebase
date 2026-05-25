/*
** ████████████████████████████████████████████
** █▄─██─▄█─▄▄▄▄█▄─▄▄─█▄─▄─▀██▀▄─██─▄▄▄▄█▄─▄▄─█
** ██─██─██▄▄▄▄─██─▄█▀██─▄─▀██─▀─██▄▄▄▄─██─▄█▀█
** ▀▀▄▄▄▄▀▀▄▄▄▄▄▀▄▄▄▄▄▀▄▄▄▄▀▀▄▄▀▄▄▀▄▄▄▄▄▀▄▄▄▄▄▀
*/
package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.ParameterizedObjectDefinition;
import com.doublegsoft.jcommons.metamodel.StatementDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.TestBase;
import io.doublegsoft.usebase.Usebase;
import io.doublegsoft.usebase.projection.ProjectionBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class HumanResourceManagementTest extends TestBase {

  private static final String OUTPUT = "out/usebase/hrm.modelbase";

  public static final String OUTPUT_DIR = "out/java/usebase-env-java/src/main/java/biz/doublegsoft/" + PROJ_NAME + "/service";

  @BeforeClass
  public static void initialize() throws Exception {
    new FileOutputStream(OUTPUT).close();
  }

  @Before
  public void test_gen_infos() throws Exception {
    ModelDefinition dataModel = loadModel("business/hrm", "business/sms");
    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);

    List<ObjectDefinition> infos = new ArrayList<>();
    for (ObjectDefinition obj : dataModel.getObjects()) {
      ObjectDefinition objInfo = projBuilder.build(obj);
      infos.add(objInfo);
    }
    printModelbaseExtensionByUsecase(OUTPUT, null, dataModel, infos.toArray(new ObjectDefinition[0]));
  }

  /**
   * 业务场景：
   * <p>
   * 雇佣员工。
   * <ul>
   *   <li>1. 通过员工标识查询是否存在该员工，如果传入的员工标识为空，则直接为空</li>
   *   <li>2. 如果员工不存在，则通过名字和身份证号查询是否赢存在员工，不存在则创建，存在则更新</li>
   *   <li>3. 保存雇佣关系，同样也需要判断是否已经存在雇佣关系</li>
   * </ul>
   */
  @Test
  public void test_hrm_onboard_new_employee() throws Exception {
    ModelDefinition dataModel = loadModel("business/hrm", "business/sms");
    String expr =
        "@onboard_new_employee({employee}) \n" +
        "|&| existing = {employee}#(name = employee_name, national_id = national_id) \n" +
        "|?| existing == null \n" +
        "|?|+| {employee}#(employee_name, national_id) \n" +
        "|+| {employment: employee = employee_id, start_date = now, status = 'E'} \n";
    Usebase usebase = new Usebase(dataModel);
    UsecaseDefinition usecase = usebase.parse(expr).get(0);
    ParameterizedObjectDefinition paramObj = usecase.getParameterizedObject();
    Assert.assertEquals("employee", paramObj.getLabelledOptions("original").get("object"));
    StatementDefinition stmtSave = usecase.getStatements().get(0);

//    printSourcesForUsecase(usecase, dataModel, OUTPUT, OUTPUT_DIR);
  }

  /**
   * 业务场景：
   * <p>
   * 员工离职。
   * <ul>
   *   <li>1. 更新员工状态，需要判断员工必须存在</li>
   *   <li>2. 更新雇佣关系状态，需要判断雇佣关系必须存在</li>
   * </ul>
   */
  @Test
  public void test_hrm_offboard_employee() throws Exception {
    ModelDefinition dataModel = loadModel("business/hrm", "business/sms");
    String expr =
        "@offboard_employee({employee: id}) \n" +
        "|:| emp = {employee}#(id = employee_id)!'员工不存在' \n" +
        "|=| {employment: end_date = now, status = 'D'}#(employee_id = employee_id, status = 'E')!'雇佣关系不存在' \n";
    Usebase usebase = new Usebase(dataModel);
    UsecaseDefinition usecase = usebase.parse(expr).get(0);
    ParameterizedObjectDefinition paramObj = usecase.getParameterizedObject();
    Assert.assertEquals("employee", paramObj.getLabelledOptions("original").get("object"));
    StatementDefinition stmtSave = usecase.getStatements().get(0);

    printModelbaseExtensionByUsecase(OUTPUT, usecase, dataModel);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

  @Test
  public void test_hrm_smart_punch() throws Exception {
    ModelDefinition dataModel = loadModel("business/hrm", "business/sms");
    String expr =
        "@clock_out({employee: employee_id}) \n" +
        "|:| emp = {employee}#(id = employee_id)!'员工不存在'" +
        "|:| attended = {attendance}#(employee_id = employee_id, work_date = now) \n" +
        "|?| attended == null \n" +
        "|?|+| {attendance: check_in_time = now, work_date = now, employee = employee_id} \n" +
        "\n" +
        // 不用着急打下班卡，需要一个机制来判断下班卡
        "|?| attended != null \n" +
        "|?|=| {attendance: check_out_time = now}#(id = attended.id) \n";
    Usebase usebase = new Usebase(dataModel);
    UsecaseDefinition usecase = usebase.parse(expr).get(0);
    ParameterizedObjectDefinition paramObj = usecase.getParameterizedObject();
    Assert.assertEquals("employee", paramObj.getLabelledOptions("original").get("object"));
    Assert.assertEquals(1, paramObj.getAttributes().length);
    Assert.assertEquals("employee_id", paramObj.getAttributes()[0].getName());

//    printSourcesForUsecase(usecase, dataModel, OUTPUT, OUTPUT_DIR);
  }

  @Test
  public void test_hrm_adjust_salary() throws Exception {
    ModelDefinition dataModel = loadModel("business/hrm", "business/sms");
    String expr =
        "@adjust_salary({employee_salary: employee_id, amount!, change_reason, effective_start_date}) \n" +
        "|:| emp = {employee}#(id = employee_id)!'员工不存在' \n" +
        "|?| effective_start_date < now !'生效日期不能晚于今天' \n" +
        "|:| future_sal = {employee_salary}#(employee = employee_id, effective_start_date > now) \n" +
        "|?| future_sal == null !'该员工已存在待生效的未来调薪计划，请先取消' \n" +
        "|:| current_sal = {employee_salary}#(employee = employee_id, is_current = true)!'找不到员工当前的薪资记录，无法调薪' \n" +
        "|?| amount != current_sal.amount !'调薪金额与当前工资相同，无需调整' \n" +
        "|+| {employee_salary: employee = employee_id, amount = amount, " +
        "    change_reason = change_reason, effective_start_date = effective_start_date} \n";
    Usebase usebase = new Usebase(dataModel);
    UsecaseDefinition usecase = usebase.parse(expr).get(0);

//    printSourcesForUsecase(usecase, dataModel, OUTPUT, OUTPUT_DIR);
  }

  @Test
  public void test_hrm_transfer_job() throws Exception {
    ModelDefinition dataModel = loadModel("business/hrm", "business/sms");
    String expr =
        "@transfer_job({job_history: employee_id!, effective_start_date!, change_reason}, {department: department_id!}, {designation: designation_id!}) \n" +
        "|:| emp = {employee}#(id = employee_id)!'员工不存在' \n" +
        "|:| dept = {department}#(id = department_id) !'目标部门不存在' \n" +
        "|:| desg = {designation}#(id = designation_id) !'目标职位不存在' \n" +
        "|:| curr_job = {job_history}#(employee = employee_id, is_current = true) !'找不到员工当前任职信息' \n" +
        "|?| curr_job.department == department_id !'调岗部门未发生变化，无需操作' \n" +
        "|?| curr_job.designation == designation_id !'调岗职位未发生变化，无需操作' \n" +
        "|?| effective_start_date >= now !'调岗生效日期不能早于今天' \n" +
        "|:| future_job = {job_history}#(employee = employee_id, start_date > today) \n" +
        "|?| future_job == null !'该员工存在尚未生效的未来调岗计划，请先撤销' \n" +
        "|+| {job_history: employee = employee_id, department = department_id, designation = designation_id, \n" +
        "    effective_start_date = effective_start_date, is_current = true, change_reason = change_reason}\n";
    Usebase usebase = new Usebase(dataModel);
    UsecaseDefinition usecase = usebase.parse(expr).get(0);

//    printSourcesForUsecase(usecase, dataModel, OUTPUT, OUTPUT_DIR);
  }

}
