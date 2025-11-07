package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.AssignmentDefinition;
import com.doublegsoft.jcommons.metamodel.ParameterizedObjectDefinition;
import com.doublegsoft.jcommons.metamodel.StatementDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.SpecBase;
import io.doublegsoft.usebase.Usebase;
import io.doublegsoft.usebase.modelbase.ModelbaseWriter;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringWriter;

public class IdentityAndAccessManagementSpec extends SpecBase {

  /**
   * 查询用户。
   */
  @Test
  public void test_find_users() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@find_users({user: username, email, status}, {role: name}):[{user: username, email} <> :role_count%count[{user_role}]%]";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    ObjectDefinition obj = usecase.getParameterizedObject();
    Assert.assertEquals("username", obj.getAttributes()[0].getName());
    Assert.assertEquals("email", obj.getAttributes()[1].getName());
    Assert.assertEquals("status", obj.getAttributes()[2].getName());

    ObjectDefinition ret = usecase.getReturnedObject();
    Assert.assertEquals("username", ret.getAttributes()[0].getName());
    Assert.assertEquals("email", ret.getAttributes()[1].getName());

    printUsecaseForModelbase(usecase);
  }

  @Test
  public void test_save_user() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@save_user({user} <> roles[user_role]#(user, role)):{user: id}";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    ParameterizedObjectDefinition paramObj = usecase.getParameterizedObject();
    Assert.assertEquals("$save_user", paramObj.getName());

    Assert.assertEquals("id", paramObj.getAttributes()[0].getName());
    Assert.assertEquals("username", paramObj.getAttributes()[1].getName());
    Assert.assertEquals("password", paramObj.getAttributes()[2].getName());
    Assert.assertEquals("email", paramObj.getAttributes()[3].getName());
    Assert.assertEquals("status", paramObj.getAttributes()[4].getName());

    ObjectDefinition ret = usecase.getReturnedObject();
    Assert.assertEquals("id", ret.getAttributes()[0].getName());
  }

  /**
   * 禁用用户。
   */
  @Test
  public void test_disable_user() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@disable_user({user: status = 'D'}#(id)):{user: id}";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    ObjectDefinition obj = usecase.getParameterizedObject();
    Assert.assertEquals("status", obj.getAttributes()[0].getName());
    printUsecaseForModelbase(usecase);
  }

  /**
   * 激活用户。
   */
  @Test
  public void test_enable_user() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@enable_user({user: status = 'D'}#(id)):{user: id}";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    ObjectDefinition obj = usecase.getParameterizedObject();
    Assert.assertEquals("status", obj.getAttributes()[0].getName());
    printUsecaseForModelbase(usecase);
  }

  /**
   * 业务场景：
   * <p>
   * 用户登录。
   * <ul>
   *   <li>1. 加密用户输入的密码</li>
   *   <li>2. 通过用户名和加密后的密码从数据库中找用户，不存在在则抛出错误</li>
   *   <li>3. 判断输入的验证码是否和会话中的验证码匹配，不匹配则抛出错误</li>
   * </ul>
   */
  @Test
  public void test_login() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@login({user: username!, password!}, captcha!):{user} \n" +
        "|=| encrypted_password = @bcrypt(password) \n" +
        "|&| user = {user}#(username, encrypted_password)!'用户名与密码错误！'\n" +
        "|?| captcha != @get_captcha_from_session('captcha') !'验证码错误' \n" +
        "|@| @put_user_into_session(#session, user) \n" +
        "|.| user";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    ObjectDefinition obj = usecase.getParameterizedObject();

    AttributeDefinition username = obj.getAttributes()[0];
    Assert.assertEquals("username", username.getName());
    Assert.assertFalse(username.getConstraint().isNullable());

    AttributeDefinition password = obj.getAttributes()[1];
    Assert.assertEquals("password", password.getName());
    Assert.assertFalse(password.getConstraint().isNullable());

    AttributeDefinition captcha = obj.getAttributes()[2];
    Assert.assertEquals("captcha", captcha.getName());
    Assert.assertFalse(captcha.getConstraint().isNullable());

    obj = usecase.getReturnedObject();
    Assert.assertEquals("user", obj.getAttributes()[0].getLabelledOptions("original").get("object"));

    Assert.assertEquals(5, usecase.getStatements().size());
    StatementDefinition stmt = usecase.getStatements().get(0);
    AssignmentDefinition assign = (AssignmentDefinition) stmt;
    Assert.assertEquals("encrypted_password", assign.getAssignee());

    printUsecaseForModelbase(usecase);
  }

  /**
   * 用户登出。
   */
  @Test
  public void test_logout() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@logout({user: id}) \n" +
        "|@| @remove_user_from_session(#session, id) \n";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    ObjectDefinition obj = usecase.getParameterizedObject();
    Assert.assertEquals("id", obj.getAttributes()[0].getName());
    obj = usecase.getReturnedObject();
    Assert.assertNull("没有返回值的定义才是正确的", obj);

    printUsecaseForModelbase(usecase);
  }

  /**
   * 业务场景：
   * <p>
   * 新添加人员的时候同时创建用户，
   * “#(national_id, person_name)”说明通过身份证号和人名控制重复录入，
   * “<person_id=user_id>”说明人员标识和用户标识构成一对一关系；
   * 默认通过标识判断新增或者更新，同时保存日志（业务无关，系统相关）。
   */
  @Test
  public void test_iam_save_person_user_roles() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    String expr =
        "@save({person}#(national_id, person_name) <person_id=user_id> {user} <user_role> [role])" +
        "|+| {audit_log: name = person_name, audit_time = now, modifier_id = 'SYS'}";
//    Usebase usebase = new Usebase(dataModel);
//    UsecaseDefinition usecase = usebase.parse(expr).get(0);
//
//    UsebaseAggregate agg = (UsebaseAggregate) usecase.getArguments().get(0).getValue();
//    Assert.assertEquals("person", agg.getPrimaryObject().getName());
//    Assert.assertEquals(2, agg.getObjects().size());
//    Assert.assertEquals("person", agg.getObjects().get(0).getName());
//    Assert.assertEquals("user", agg.getObjects().get(1).getName());
//
//    List<UsebaseArgument> args = agg.getObjects().get(0).getFilterArguments();
//    Assert.assertEquals(2, args.size());
//    Assert.assertEquals("national_id", args.get(0).getName());
//    Assert.assertEquals("person_name", args.get(1).getName());

//    UsebaseSave stmt0 = (UsebaseSave) usecase.getStatements().get(0);
//    Assert.assertEquals("audit_log", stmt0.getTypeName());
//    Assert.assertEquals("name", stmt0.getSelectedAttributes().get(0).getName());
//    Assert.assertEquals("person_name", stmt0.getSelectedAttributes().get(0).getDefaultValue().getVariable());
//    Assert.assertEquals("audit_time", stmt0.getSelectedAttributes().get(1).getName());
//    Assert.assertEquals("now", stmt0.getSelectedAttributes().get(1).getDefaultValue().getKeyword());
//    Assert.assertEquals("modifier_id", stmt0.getSelectedAttributes().get(2).getName());
//    Assert.assertEquals("SYS", stmt0.getSelectedAttributes().get(2).getDefaultValue().getString());
  }

  /**
   * 业务场景：
   * <p>
   * 新添加人员的时候同时创建用户，
   * “#(national_id, person_name)”说明通过身份证号和人名控制重复录入，
   * “<person_id=user_id>”说明人员标识和用户标识构成一对一关系；
   * 默认通过标识判断新增或者更新，在远程日志服务器上保存日志（业务无关，系统相关）。
   */
  @Test
  public void test_iam_save_person_user_roles_and_remote_log() throws Exception {
    String expr =
        "@save({person}#(national_id, person_name) <person_id=user_id> {user} <user_role> [role])" +
            "  |+| {audit_log: name = person_name, audit_time = now, modifier_id = 'SYS'} " +
            "      @http://log.storage.com/audit";
//    Usebase usebase = new Usebase();
//    UsebaseUsecase usecase = usebase.parse(expr).get(0);
//    UsebaseSave stmt0 = (UsebaseSave) usecase.getStatements().get(0);
//    UsebaseRemote remote = stmt0.getRemote();
//    Assert.assertEquals("http", remote.getScheme());
//    Assert.assertEquals("log.storage.com", remote.getHost());
//    Assert.assertEquals("/audit", remote.getUri());
  }

}
