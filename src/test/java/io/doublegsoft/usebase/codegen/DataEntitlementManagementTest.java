package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import io.doublegsoft.usebase.TestBase;
import org.junit.Test;

public class DataEntitlementManagementTest extends TestBase {

  @Test
  public void test() throws Exception {
    ModelDefinition dataModel = loadDataModel("business/dem");
  }

}
