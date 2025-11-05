package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import io.doublegsoft.usebase.SpecBase;
import org.junit.Test;

public class AutomatedAdministrationManagementSpec extends SpecBase {

  @Test
  public void test_save_knowledge() throws Exception {
    ModelDefinition dataModel = loadModel("aam");
  }

}
