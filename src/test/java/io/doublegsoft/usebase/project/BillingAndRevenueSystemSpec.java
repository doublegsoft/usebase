package io.doublegsoft.usebase.project;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingAndRevenueSystemSpec extends SpecBase {

  public static final String BNR_ROOT = "/Users/christian/export/local/works/doublegsoft.biz/stdbiz/05.Testing/BNR";

  @BeforeClass
  public static void initialize() throws Exception {
    new FileOutputStream(BNR_ROOT + "/doc/bnr4u.modelbase").close();
  }

  @Before
  public void test_gen_infos() throws Exception {
    ModelDefinition dataModel = loadModel(BNR_ROOT + "/doc/bnr.modelbase");
    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);

    List<ObjectDefinition> infos = new ArrayList<>();
    for (ObjectDefinition obj : dataModel.getObjects()) {
      ObjectDefinition rowObj = projBuilder.build(obj);
      infos.add(rowObj);
    }
    printModelbaseExtensionByUsecase(BNR_ROOT + "/doc/bnr4u.modelbase", null, infos.toArray(new ObjectDefinition[0]));
  }

  @Test
  public void test_bnr() throws Exception {
    ModelDefinition dataModel = loadModel(BNR_ROOT + "/doc/bnr.modelbase");
    String usebaseExpr = loadUsebaseExpression(BNR_ROOT + "/doc/bnr.usebase");
    List<UsecaseDefinition> usecases = new Usebase(dataModel).parse(usebaseExpr);
    Assert.assertFalse(usecases.isEmpty());

    Map<String, Object> data = new HashMap<>();
    data.put("namespace", "com.helloworld");
    for (UsecaseDefinition usecase : usecases) {
      generateCode("bnr", data, BNR_ROOT + "/doc/bnr4u.modelbase",
          BNR_ROOT + "/gen/bnr/src/main/java/com/helloworld/bnr",
          usecase, dataModel);
    }
    Assert.assertTrue(bash(BNR_ROOT + "/gen-u.sh"));
  }

}
