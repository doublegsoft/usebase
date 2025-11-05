/*
** ████████████████████████████████████████████
** █▄─██─▄█─▄▄▄▄█▄─▄▄─█▄─▄─▀██▀▄─██─▄▄▄▄█▄─▄▄─█
** ██─██─██▄▄▄▄─██─▄█▀██─▄─▀██─▀─██▄▄▄▄─██─▄█▀█
** ▀▀▄▄▄▄▀▀▄▄▄▄▄▀▄▄▄▄▄▀▄▄▄▄▀▀▄▄▀▄▄▀▄▄▄▄▄▀▄▄▄▄▄▀
*/
package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metamodel.ParameterizedObjectDefinition;
import com.doublegsoft.jcommons.metamodel.StatementDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.SpecBase;
import io.doublegsoft.usebase.Usebase;
import org.junit.Assert;
import org.junit.Test;

public class HumanResourceManagementSpec extends SpecBase {

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
  public void test_hrm_hire_employee() throws Exception {
    ModelDefinition dataModel = loadModel("hrm");
    String expr =
        "@hire_employee({employee}) \n" +
        "|&| existing = {employee}#({employee: id}) \n" +
        "|?| existing == null \n" +
        "|?|+| employee#(name, national_id) \n" +
        "|+| {employment: employee = employee, start_date = now, state = 'E'}#(employee = employee, state = 'E') \n";
    Usebase usebase = new Usebase(dataModel);
    UsecaseDefinition usecase = usebase.parse(expr).get(0);
    ParameterizedObjectDefinition paramObj = usecase.getParameterizedObject();
    Assert.assertEquals("employee", paramObj.getLabelledOptions("original").get("object"));
    StatementDefinition stmtSave = usecase.getStatements().get(0);
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
  public void test_hrm_leave_company() throws Exception {
    ModelDefinition dataModel = loadModel("hrm");
    String expr =
        "@leave_company({employee}) \n" +
        "|+| {employee: state = 'D'}#(employee)!'员工不存在' \n" +
        "|+| {employment: end_date = now, state = 'D'}#(employee = employee, state = 'E')!'雇佣关系不存在' \n";
    Usebase usebase = new Usebase(dataModel);
    UsecaseDefinition usecase = usebase.parse(expr).get(0);
    ParameterizedObjectDefinition paramObj = usecase.getParameterizedObject();
    Assert.assertEquals("employee", paramObj.getLabelledOptions("original").get("object"));
    StatementDefinition stmtSave = usecase.getStatements().get(0);

  }

  /**
   * 业务场景：
   * <p>
   * 上班打卡。
   * <ul>
   *   <li>1. 员工上班打卡，需要判断员工必须存在</li>
   * </ul>
   */
  @Test
  public void test_hrm_punch_in() throws Exception {
    ModelDefinition dataModel = loadModel("hrm");
    String expr =
        "@punch_in({employee: id}) \n" +
        "|+| {attendance: check_in_time = now, employee = id}#(id)!'员工不存在' \n";
    Usebase usebase = new Usebase(dataModel);
    UsecaseDefinition usecase = usebase.parse(expr).get(0);
    ParameterizedObjectDefinition paramObj = usecase.getParameterizedObject();
    Assert.assertEquals("employee", paramObj.getLabelledOptions("original").get("object"));
    Assert.assertEquals(1, paramObj.getAttributes().length);
    Assert.assertEquals("id", paramObj.getAttributes()[0].getName());
  }

  @Test
  public void test_hrm_punch_out() throws Exception {
    ModelDefinition dataModel = loadModel("hrm");
    String expr =
        "@punch_out({employee: id}) \n" +
        "|+| {attendance: check_out_time = now, employee = id}#(id)!'员工不存在' \n";
    Usebase usebase = new Usebase(dataModel);
    UsecaseDefinition usecase = usebase.parse(expr).get(0);
    ParameterizedObjectDefinition paramObj = usecase.getParameterizedObject();
    Assert.assertEquals("employee", paramObj.getLabelledOptions("original").get("object"));
    Assert.assertEquals(1, paramObj.getAttributes().length);
    Assert.assertEquals("id", paramObj.getAttributes()[0].getName());
  }

}
