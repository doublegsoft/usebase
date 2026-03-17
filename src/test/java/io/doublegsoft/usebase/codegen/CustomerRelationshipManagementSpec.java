package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.ParameterizedObjectDefinition;
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
import java.util.ArrayList;
import java.util.List;

public class CustomerRelationshipManagementSpec extends SpecBase {

  private static final String OUTPUT = "out/usebase/crm.modelbase";

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
      ObjectDefinition objInfo = projBuilder.build(obj);
      infos.add(objInfo);
    }
    printModelbaseExtensionByUsecase(OUTPUT, null, infos.toArray(new ObjectDefinition[0]));
  }

  @Test
  public void test_hrm_onboard_new_employee() throws Exception {
    ModelDefinition dataModel = loadModel("crm", "sms");
//    String expr =
//        "@onboard_new_employee({employee}) \n" +
//            "|&| existing = {employee}#(name = employee_name, national_id = national_id) \n" +
//            "|?| existing == null \n" +
//            "|?|+| {employee}#(employee_name, national_id) \n" +
//            "|+| {employment: employee = employee_id, start_date = now, status = 'E'} \n";
//    Usebase usebase = new Usebase(dataModel);
//    UsecaseDefinition usecase = usebase.parse(expr).get(0);
//
//    printModelbaseExtensionByUsecase(OUTPUT, usecase);
//    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
//        usecase, dataModel, OUTPUT_DIR + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
//    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
//        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
//    printJavaCodeForUsecase(TEMPLATE_SERVICE,
//        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }


}
