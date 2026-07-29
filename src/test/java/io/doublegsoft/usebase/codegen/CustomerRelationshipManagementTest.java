package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.TestBase;
import io.doublegsoft.usebase.Usebase;
import io.doublegsoft.usebase.projection.ProjectionBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class CustomerRelationshipManagementTest extends TestBase {

  private static final String OUTPUT = "out/usebase/crm.modelbase";

  public static final String OUTPUT_DIR = "out/java/usebase-env-java/src/main/java/biz/doublegsoft/" + PROJ_NAME + "/service";

  @BeforeClass
  public static void initialize() throws Exception {
    new FileOutputStream(OUTPUT).close();
  }

  @Before
  public void test_gen_infos() throws Exception {
    ModelDefinition dataModel = loadDataModel("business/crm", "business/sms");
    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);

    List<ObjectDefinition> infos = new ArrayList<>();
    for (ObjectDefinition obj : dataModel.getObjects()) {
      ObjectDefinition rowObj = projBuilder.build(obj);
      infos.add(rowObj);
    }
    printModelbaseExtensionByUsecase(OUTPUT, null, dataModel, infos.toArray(new ObjectDefinition[0]));
  }

  @Test
  public void test_crm_convert_sales_lead() throws Exception {
    ModelDefinition dataModel = loadDataModel("business/crm", "business/sms");
    String expr = loadUsebaseExpression("business/crm/convert_sales_lead.usebase");
    Usebase usebase = new Usebase(dataModel);
    UsecaseDefinition usecase = usebase.parse(expr).get(0);
//    printSourcesForUsecase(usecase, dataModel, OUTPUT, OUTPUT_DIR);
  }

  @Test
  public void test_crm_log_sales_activity() throws Exception {
    ModelDefinition dataModel = loadDataModel("business/crm", "business/sms");
    String expr = loadUsebaseExpression("business/crm/log_sales_activity.usebase");
    Usebase usebase = new Usebase(dataModel);
    UsecaseDefinition usecase = usebase.parse(expr).get(0);
//    printSourcesForUsecase(usecase, dataModel, OUTPUT, OUTPUT_DIR);
  }

  @Test
  public void test_crm_win_opportunity() throws Exception {
    ModelDefinition dataModel = loadDataModel("business/crm", "business/sms");
    String expr = loadUsebaseExpression("business/crm/win_opportunity.usebase");
    Usebase usebase = new Usebase(dataModel);
    UsecaseDefinition usecase = usebase.parse(expr).get(0);
//    printSourcesForUsecase(usecase, dataModel, OUTPUT, OUTPUT_DIR);
  }

}
