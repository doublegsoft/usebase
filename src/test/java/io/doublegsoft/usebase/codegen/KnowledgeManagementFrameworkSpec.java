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
import java.util.ArrayList;
import java.util.List;

public class KnowledgeManagementFrameworkSpec extends SpecBase {

  private static final String OUTPUT = "out/usebase/kmf.modelbase";

  public static final String OUTPUT_DIR = "out/java/usebase-env-java/src/main/java/biz/doublegsoft/" + PROJ_NAME + "/service";


  @BeforeClass
  public static void initialize() throws Exception {
    new FileOutputStream(OUTPUT).close();
  }

  @Before
  public void test_kmf_gen_infos() throws Exception {
    ModelDefinition dataModel = loadModel("kmf");
    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);

    List<ObjectDefinition> infos = new ArrayList<>();
    for (ObjectDefinition obj : dataModel.getObjects()) {
      ObjectDefinition objInfo = projBuilder.build(obj);
      infos.add(objInfo);
    }
    printModelbaseExtensionByUsecase(OUTPUT, null, infos.toArray(new ObjectDefinition[0]));
  }

  /**
   * 保存知识。
   */
  @Test
  public void test_kmf_save_knowledge() throws Exception {
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
  public void test_kmf_save_knowledge_entry() throws Exception {
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

//  @Test
//  public void test_kmf_submit_feedback() throws Exception {
//    ModelDefinition dataModel = loadModel("kmf");
//    String expr =
//        "@submit_feedback({article: article_id!, title!}):{feedback: id} \n" +
//        "|:| art = {article}#(id = article_id) !'文章不存在' \n" +
//        "|?| assert(score >= 1 AND score <= 5) !'评分必须在1到5之间' \n" +
//        "|:| past_fb = {feedback}#(article = article_id, user = user_id) \n" +
//        "|?| assert(past_fb == null) !error '您已经评价过该文章' \n" +
//        "|+| {feedback: article = article_id, user = user_id, score = score, comment = comment} \n" +
//        "|?| score <= 2 \n" +
//        "|?|:| ";
//    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
//    ObjectDefinition objArg = usecase.getParameterizedObject();
//    Assert.assertEquals("knowledge_entry",
//        objArg.getLabelledOptions("original").get("object"));
//    Assert.assertFalse("知识选项不能为空",
//        objArg.getAttributes()[0].getConstraint().isNullable());
//    Assert.assertFalse("标题不能为空",
//        objArg.getAttributes()[1].getConstraint().isNullable());
//
//    ObjectDefinition ret = usecase.getReturnedObject();
//    Assert.assertEquals("knowledge_entry", ret.getAttributes()[0].getLabelledOptions("original").get("object"));
//    Assert.assertEquals("knowledge_entry_id", ret.getAttributes()[0].getName());
//
//    printModelbaseExtensionByUsecase(OUTPUT, usecase);
//    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
//        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
//    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
//        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
//    printJavaCodeForUsecase(TEMPLATE_SERVICE,
//        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
//  }
}
