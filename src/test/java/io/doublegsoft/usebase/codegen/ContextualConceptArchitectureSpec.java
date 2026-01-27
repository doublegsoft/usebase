package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.SpecBase;
import io.doublegsoft.usebase.Usebase;
import org.junit.Test;

public class ContextualConceptArchitectureSpec extends SpecBase {

  @Test
  public void test_cca_datamodel() throws Exception {
    ModelDefinition dataModel = loadModel("cca");
    ModelDefinition apiModel = new ModelDefinition();
//    String expr = "";
//    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
//    checkOriginalIndexAndObject(usecase.getReturnedObject());
  }

}
