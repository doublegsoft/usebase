package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import io.doublegsoft.usebase.SpecBase;
import org.junit.Test;

public class BusinessPolicyEngineSpec extends SpecBase {

  @Test
  public void test() throws Exception {
    ModelDefinition dataModel = loadModel("bpe");
  }

}
