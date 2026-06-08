package io.doublegsoft.usebase.business;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import io.doublegsoft.usebase.TestBase;
import org.junit.Test;

public class RateEngineTest extends TestBase {

  @Test
  public void test_only_master() throws Exception {
    ModelDefinition dataModel = loadModel("business/rateengine");
    System.out.println(dataModel.getObjects().length);
  }
}
