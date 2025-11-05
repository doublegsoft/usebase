/*
** ████████████████████████████████████████████
** █▄─██─▄█─▄▄▄▄█▄─▄▄─█▄─▄─▀██▀▄─██─▄▄▄▄█▄─▄▄─█
** ██─██─██▄▄▄▄─██─▄█▀██─▄─▀██─▀─██▄▄▄▄─██─▄█▀█
** ▀▀▄▄▄▄▀▀▄▄▄▄▄▀▄▄▄▄▄▀▄▄▄▄▀▀▄▄▀▄▄▀▄▄▄▄▄▀▄▄▄▄▄▀
*/
package io.doublegsoft.usebase.codegen;

import org.junit.Test;

public class BusinessProcessAutomationSpec {

  /**
   * 业务场景：
   * <p>
   * 系统自动化节点，通知供应商
   */
  @Test
  public void test_bpm_notify_supplier() throws Exception {
    String expr =
        "@notify_supplier({product: product_id} <> {order_line: amount})" +
            "@http://www.microsoft.com/api/supply";
//    Usebase usebase = new Usebase();
//    UsebaseUsecase usecase = usebase.parse(expr).get(0);
//    UsebaseRemote remote = usecase.getRemote();
//    UsebaseAggregate agg = (UsebaseAggregate)usecase.getArguments().get(0).getValue();
//    UsebaseObject obj = agg.getPrimaryObject();
//    Assert.assertEquals("product", obj.getName());
//    Assert.assertEquals("product_id", obj.getSelectedAttributes().get(0).getName());
//
//    Assert.assertEquals("order_line", agg.getObjects().get(1).getName());
//    Assert.assertEquals("http", remote.getScheme());
//    Assert.assertEquals("www.microsoft.com", remote.getHost());
//    Assert.assertEquals("/api/supply", remote.getUri());
  }

  /**
   * 业务场景：
   * <p>
   * 系统自动化节点，通过第三方支付订单
   */
  @Test
  public void test_bpm_pay_order() throws Exception {
    String expr =
        "@pay_order({order: order_no, amount})@http://www.alipay.com/api/pay";
//    Usebase usebase = new Usebase();
//    UsebaseUsecase usecase = usebase.parse(expr).get(0);
//    UsebaseRemote remote = usecase.getRemote();
//    UsebaseAggregate agg = (UsebaseAggregate)usecase.getArguments().get(0).getValue();
//    UsebaseObject obj = agg.getPrimaryObject();
//    Assert.assertEquals("order", obj.getName());
//    Assert.assertEquals("order_no", obj.getSelectedAttributes().get(0).getName());
//    Assert.assertEquals("amount", obj.getSelectedAttributes().get(1).getName());
//    Assert.assertEquals("http", remote.getScheme());
//    Assert.assertEquals("www.alipay.com", remote.getHost());
//    Assert.assertEquals("/api/pay", remote.getUri());
  }

}
