package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import io.doublegsoft.usebase.TestBase;
import org.junit.Test;

public class AutomatedAdministrationManagementTest extends TestBase {

  @Test
  public void test_save_knowledge() throws Exception {
    ModelDefinition dataModel = loadDataModel("business/aam");
  }

}
