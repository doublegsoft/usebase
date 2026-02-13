package io.doublegsoft.usebase.grammar;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.SpecBase;
import io.doublegsoft.usebase.Usebase;
import org.junit.Test;

public class CalcExprTest extends SpecBase {

  @Test
  public void test() throws Exception {
    ModelDefinition dataModel = loadModel("abpms");
    String expr =
        "@generate_bill({account: account_id!}):{bill}\n" +
            "amount = %{meter_read: usage}#(service_agreement.rate_type, status = 'E') * " +
            "{cfg_rate: rate}#(service_agreement.rate_type) + 100%}]&service_agreements";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
  }

}
