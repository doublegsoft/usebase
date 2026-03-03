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
import io.doublegsoft.usebase.aggregate.AggregateBuilder;
import io.doublegsoft.usebase.aggregate.AggregateRelationshipChain;
import io.doublegsoft.usebase.aggregate.ObjectRelationships;
import io.doublegsoft.usebase.aggregate.Relationship;
import io.doublegsoft.usebase.association.AssociationBuilder;
import io.doublegsoft.usebase.association.AssociationChain;
import io.doublegsoft.usebase.projection.ProjectionBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class IdentityAndAccessManagementSpec extends SpecBase {

  private static final String OUTPUT = "out/usebase/iam.modelbase";

  public static final String OUTPUT_DIR = "out/java/iam/src/main/java/biz/doublegsoft/iam/service";

  @BeforeClass
  public static void initialize() throws Exception {
    new FileOutputStream(OUTPUT).close();
  }

  @Before
  public void test_gen_infos() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);

    ObjectDefinition user = dataModel.findObjectByName("user");
    ObjectDefinition userInfo = projBuilder.build(user);
    ObjectDefinition userRole = dataModel.findObjectByName("user_role");
    ObjectDefinition userRoleInfo = projBuilder.build(userRole);
    ObjectDefinition permission = dataModel.findObjectByName("permission");
    ObjectDefinition permissionInfo = projBuilder.build(permission);
    ObjectDefinition role = dataModel.findObjectByName("role");
    ObjectDefinition roleInfo = projBuilder.build(role);
    ObjectDefinition policy = dataModel.findObjectByName("policy");
    ObjectDefinition policyInfo = projBuilder.build(policy);

    printModelbaseExtensionByUsecase(OUTPUT, null,
        userInfo, userRoleInfo, permissionInfo, roleInfo, policyInfo);
  }

  /**
   * 查询用户。
   */
  @Test
  public void test_find_users() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@find_users({user: username, email, status}, {role: name}):[{user: user_id, username, email} <> :role_count%count[{user_role}]%]";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("iam");

    ObjectDefinition obj = usecase.getParameterizedObject();
    Assert.assertEquals("username", obj.getAttributes()[0].getName());
    Assert.assertEquals("email", obj.getAttributes()[1].getName());
    Assert.assertEquals("status", obj.getAttributes()[2].getName());

    ObjectDefinition ret = usecase.getReturnedObject();
    Assert.assertEquals("user_id", ret.getAttributes()[0].getName());
    Assert.assertEquals("username", ret.getAttributes()[1].getName());
    Assert.assertEquals("email", ret.getAttributes()[2].getName());

    printModelbaseExtensionByUsecase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

  @Test
  public void test_get_user() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@get_user({user: user_id, (username, email)}):" +
            "{user: user_id, username, email} <user_role> [role] <role_permission> [permission]";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("iam");

    ObjectDefinition paramObj = usecase.getParameterizedObject();
    Assert.assertEquals("username", paramObj.getAttributes()[1].getName());
    Assert.assertEquals("email", paramObj.getAttributes()[2].getName());

    ObjectDefinition retObj = usecase.getReturnedObject();
    Assert.assertEquals("user_id", retObj.getAttributes()[0].getName());
    Assert.assertEquals("username", retObj.getAttributes()[1].getName());
    Assert.assertEquals("email", retObj.getAttributes()[2].getName());

    AggregateBuilder builder = new AggregateBuilder(dataModel);
    AggregateRelationshipChain chain = builder.build(usecase.getReturnedObject());
    Assert.assertNotNull(chain.getRelationship("user_role", "role"));
    Assert.assertNotNull(chain.getRelationship("role_permission", "role"));
    Assert.assertNotNull(chain.getRelationship("role_permission", "permission"));

    AssociationChain assoc = new AssociationBuilder(dataModel).build(paramObj, retObj);
    usecase.setOption("relations", chain);
    for (Relationship rel : chain.getRelationships()) {
      System.out.println(rel);
    }

    printModelbaseExtensionByUsecase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

  @Test
  public void test_save_user() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@save_user({user} <> roles[user_role]#(user, role)):{user: id}";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("iam");

    ParameterizedObjectDefinition paramObj = usecase.getParameterizedObject();
    Assert.assertEquals("$save_user", paramObj.getName());

    Assert.assertEquals("user_id", paramObj.getAttributes()[0].getName());
    Assert.assertEquals("username", paramObj.getAttributes()[1].getName());
    Assert.assertEquals("encrypted_password", paramObj.getAttributes()[2].getName());
    Assert.assertEquals("email", paramObj.getAttributes()[3].getName());
    Assert.assertEquals("status", paramObj.getAttributes()[4].getName());

    ObjectDefinition ret = usecase.getReturnedObject();
    Assert.assertEquals("user_id", ret.getAttributes()[0].getName());

    printModelbaseExtensionByUsecase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

  /**
   * 禁用用户。
   */
  @Test
  public void test_disable_user() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@disable_user({user: status = 'D', user_id!}):{user: id}\n" +
        "|=| {user: status = 'D'}#(user_id)\n";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("iam");
    ObjectDefinition obj = usecase.getParameterizedObject();
    Assert.assertEquals("status", obj.getAttributes()[0].getName());

    printModelbaseExtensionByUsecase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

  /**
   * 激活用户。
   */
  @Test
  public void test_enable_user() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@enable_user({user: status = 'E', user_id!}):{user: id}" +
        "|=| {user: status = 'E'}#(user_id)\n";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("iam");
    ObjectDefinition obj = usecase.getParameterizedObject();
    Assert.assertEquals("status", obj.getAttributes()[0].getName());

    printModelbaseExtensionByUsecase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
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
  public void test_iam_login() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    String expr =
        "@login({user: username!}, password!, captcha!):{user} <> :token \n" +
        "|:| encrypted_password = @bcrypt(password) \n" +
        "|:| user = {user}#(username, encrypted_password)!'用户名与密码错误！'\n" +
        "|?| captcha != @get_captcha_from_session('captcha') !'验证码错误' \n" +
        "|@| @put_user_into_session(user) \n" +
        "|:| token = @generate_token(user) \n" +
        "|.| user";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("iam");

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

    Assert.assertEquals(6, usecase.getStatements().size());
    StatementDefinition stmt = usecase.getStatements().get(0);
    AssignmentDefinition assign = (AssignmentDefinition) stmt;
    Assert.assertEquals("encrypted_password", assign.getAssignee());

    printModelbaseExtensionByUsecase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

  /**
   * 用户登出。
   */
  @Test
  public void test_iam_logout() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    String expr =
        "@logout({user: user_id}) \n" +
        "|@| @remove_user_from_session(#session, user_id) \n";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    usecase.setModule("iam");

    ObjectDefinition obj = usecase.getParameterizedObject();
    Assert.assertEquals("user_id", obj.getAttributes()[0].getName());
    obj = usecase.getReturnedObject();
    Assert.assertNull("没有返回值的定义才是正确的", obj);

    printModelbaseExtensionByUsecase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
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
  public void save_user_with_roles() throws Exception {
    ModelDefinition dataModel = loadModel("sms", "iam");
    String expr =
        "@save_user_with_roles({person}#(national_id!, person_name!) <person.id=user.id> {user} <user_role> [role]) \n" +
        "|?| @validate_national_id_and_name(national_id, person_name)!'身份证号和姓名不匹配' \n" +
        "|+| {person} \n" +
        "|+| {user} \n" +
        "|+| [user_role] \n";
    Usebase usebase = new Usebase(dataModel);
    UsecaseDefinition usecase = usebase.parse(expr).get(0);
    usecase.setModule("iam");

    printModelbaseExtensionByUsecase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

}