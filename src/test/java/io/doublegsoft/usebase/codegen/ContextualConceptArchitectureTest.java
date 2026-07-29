package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import io.doublegsoft.usebase.TestBase;
import org.junit.Test;

public class ContextualConceptArchitectureTest extends TestBase {

  @Test
  public void test_cca_datamodel() throws Exception {
    ModelDefinition dataModel = loadDataModel("business/cca");
    ModelDefinition apiModel = new ModelDefinition();
//    String expr = "";
//    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
//    checkOriginalIndexAndObject(usecase.getReturnedObject());
  }

}
