package io.doublegsoft.usebase.business;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.PrimitiveType;
import com.doublegsoft.jcommons.metamodel.AssignmentDefinition;
import com.doublegsoft.jcommons.metamodel.CalculationDefinition;
import com.doublegsoft.jcommons.metamodel.LoopDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.TestBase;
import io.doublegsoft.usebase.Usebase;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class CashingTest extends TestBase {

  @Test
  public void test() throws Exception {
    String expr =
        "@generate_order({member: id!}, items[order_item]):{order}\n" +
        "|:| total_amount = 0 \n" +
        "|*| item in items \n" +
        "|*|:| total_amount += item.unit_price * item.quantity \n" +
        "|*|:| item.subtotal = item.unit_price * item.quantity \n" +
        "|:| order = {order: pay_amount = total_amount} \n" +
        "|+| order \n" +
        "|.| order \n";
    UsecaseDefinition usecase = new Usebase(build_data_model_cashier()).parse(expr).get(0);
    Assert.assertEquals(5, usecase.getStatements().size());
    AssignmentDefinition assign = (AssignmentDefinition) usecase.getStatements().get(0);
    Assert.assertEquals(BigDecimal.ZERO, assign.getValue().getNumber());

    LoopDefinition loop = (LoopDefinition) usecase.getStatements().get(1);
    assign = (AssignmentDefinition) loop.getStatements().get(0);
    Assert.assertEquals("total_amount", assign.getAssignee().getName());
    Assert.assertEquals(new PrimitiveType("number"), assign.getAssignee().getType());
    CalculationDefinition calcExpr = assign.getValue().getCalcExpr();
    Assert.assertEquals("unit_price", calcExpr.getLeftOperand().getValue().getAttributeValue().getName());
    Assert.assertEquals("quantity", calcExpr.getRightOperand().getValue().getAttributeValue().getName());

    assign = (AssignmentDefinition) loop.getStatements().get(1);
    Assert.assertEquals("subtotal", assign.getAssignee().getAttribute().getName());
    calcExpr = assign.getValue().getCalcExpr();
    Assert.assertEquals("unit_price", calcExpr.getLeftOperand().getValue().getAttributeValue().getName());
    Assert.assertEquals("quantity", calcExpr.getRightOperand().getValue().getAttributeValue().getName());

    assign = (AssignmentDefinition) usecase.getStatements().get(2);
    Assert.assertEquals("order", assign.getAssignee().getName());
    Assert.assertEquals("order", assign.getAssignee().getType().getName());
    Assert.assertEquals("#order", assign.getValue().getObjectValue().getName());
    Assert.assertEquals("pay_amount", assign.getValue().getObjectValue().getAttributes()[0].getName());
  }

  private ModelDefinition build_data_model_cashier() {
    ModelDefinition retVal = new ModelDefinition();

    // ==========================================
    // 1. 基础主数据
    // ==========================================

    // 1.1 商品 (product)
    ObjectDefinition productObj = createPersistentObject(retVal, "product");
    createIdentifiableAttribute(productObj, "id", new PrimitiveType("long"));
    createAttributeWithPrimitiveType(productObj, "barcode", new PrimitiveType("string")); // 条形码/编码
    createAttributeWithPrimitiveType(productObj, "name", new PrimitiveType("string"));    // 商品名称
    createAttributeWithPrimitiveType(productObj, "price", new PrimitiveType("number"));   // 零售单价
    createAttributeWithPrimitiveType(productObj, "stock", new PrimitiveType("int"));      // 库存

    // 1.2 会员 (member)
    ObjectDefinition memberObj = createPersistentObject(retVal, "member");
    createIdentifiableAttribute(memberObj, "id", new PrimitiveType("long"));
    createAttributeWithPrimitiveType(memberObj, "phone", new PrimitiveType("string"));       // 手机号
    createAttributeWithPrimitiveType(memberObj, "name", new PrimitiveType("string"));        // 会员昵称
    createAttributeWithPrimitiveType(memberObj, "level", new PrimitiveType("string"));       // 等级: 普通/黄金/钻石
    createAttributeWithPrimitiveType(memberObj, "discount", new PrimitiveType("number"));    // 折扣率 (如 0.88)
    createAttributeWithPrimitiveType(memberObj, "points", new PrimitiveType("int"));         // 积分

    // 1.3 收银员/操作员 (cashier)
    ObjectDefinition cashierObj = createPersistentObject(retVal, "cashier");
    createIdentifiableAttribute(cashierObj, "id", new PrimitiveType("long"));
    createAttributeWithPrimitiveType(cashierObj, "emp_no", new PrimitiveType("string")); // 工号
    createAttributeWithPrimitiveType(cashierObj, "name", new PrimitiveType("string"));   // 姓名

    // ==========================================
    // 2. 交易核心数据
    // ==========================================

    // 2.1 收银订单主表 (order)
    ObjectDefinition orderObj = createPersistentObject(retVal, "order");
    createIdentifiableAttribute(orderObj, "id", new PrimitiveType("long"));
    createAttributeWithPrimitiveType(orderObj, "order_no", new PrimitiveType("string"));         // 订单流水号
    createAttributeWithCustomType(orderObj, "cashier", cashierObj);                              // 操作收银员
    createAttributeWithCustomType(orderObj, "member", memberObj);                                // 关联会员(可为空)
    createAttributeWithPrimitiveType(orderObj, "total_amount", new PrimitiveType("number"));    // 商品原价总额
    createAttributeWithPrimitiveType(orderObj, "discount_amount", new PrimitiveType("number")); // 优惠金额
    createAttributeWithPrimitiveType(orderObj, "pay_amount", new PrimitiveType("number"));      // 应收/实付金额
    createAttributeWithPrimitiveType(orderObj, "status", new PrimitiveType("string"));          // 状态: 待付款/已完成/已退款

    // 2.2 订单明细项 (order_item)
    ObjectDefinition orderItemObj = createPersistentObject(retVal, "order_item");
    createIdentifiableAttribute(orderItemObj, "id", new PrimitiveType("long"));
    createAttributeWithCustomType(orderItemObj, "order", orderObj);                             // 所属订单
    createAttributeWithCustomType(orderItemObj, "product", productObj);                         // 购买商品
    createAttributeWithPrimitiveType(orderItemObj, "unit_price", new PrimitiveType("number")); // 购买时的快照单价
    createAttributeWithPrimitiveType(orderItemObj, "quantity", new PrimitiveType("int"));       // 购买数量
    createAttributeWithPrimitiveType(orderItemObj, "subtotal", new PrimitiveType("number"));   // 小计 (单价 * 数量)

    // 2.3 支付记录 (payment) - 支持组合支付 (如现金 + 微信)
    ObjectDefinition paymentObj = createPersistentObject(retVal, "payment");
    createIdentifiableAttribute(paymentObj, "id", new PrimitiveType("long"));
    createAttributeWithCustomType(paymentObj, "order", orderObj);                                 // 关联订单
    createAttributeWithPrimitiveType(paymentObj, "pay_method", new PrimitiveType("string"));     // 方式: 微信/支付宝/现金/余额
    createAttributeWithPrimitiveType(paymentObj, "amount", new PrimitiveType("number"));         // 支付金额
    createAttributeWithPrimitiveType(paymentObj, "status", new PrimitiveType("string"));         // 状态: 成功/失败/已退款

    return retVal;
  }
}
