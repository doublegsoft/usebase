package io.doublegsoft.usebase.grammar;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metamodel.AssignmentDefinition;
import com.doublegsoft.jcommons.metamodel.CalculationDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.TestBase;
import io.doublegsoft.usebase.Usebase;
import org.junit.Test;

public class CalcExprTest extends TestBase {

  @Test
  public void test() throws Exception {
    ModelDefinition dataModel = loadDataModel("business/cc&b");
    String expr =
        "@generate_bill({account: account_id!}):{bill}\n" +
        "|:| amount = % {meter_read: usage}#(service_agreement.rate_type, status = 'E') * " +
        "    {cfg_rate: rate}#(service_agreement.rate_type) + 100 %}]&service_agreements";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    AssignmentDefinition assign = (AssignmentDefinition) usecase.getStatements().get(0);
    CalculationDefinition calcExpr = assign.getValue().getCalcExpr();
//    List<ValueDefinition> operands = calcExpr.getOperands();
//    Assert.assertEquals(3, operands.size());
//
//    ValueDefinition third = operands.get(2);
//    Assert.assertEquals(new BigDecimal("100"), third.getNumber());
  }

}
