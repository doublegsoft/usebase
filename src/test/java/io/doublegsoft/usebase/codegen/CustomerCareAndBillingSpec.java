package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.*;
import io.doublegsoft.usebase.SpecBase;
import io.doublegsoft.usebase.Usebase;
import io.doublegsoft.usebase.association.AssociationBuilder;
import io.doublegsoft.usebase.association.AssociationChain;
import io.doublegsoft.usebase.projection.ProjectionBuilder;
import io.doublegsoft.usebase.aggregate.AggregateBuilder;
import io.doublegsoft.usebase.aggregate.AggregateRelationshipChain;
import io.doublegsoft.usebase.aggregate.ObjectRelationships;
import io.doublegsoft.usebase.aggregate.Relationship;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class CustomerCareAndBillingSpec extends SpecBase {

  private static final String OUTPUT = "out/cc&b.usebase";

  public static final String OUTPUT_DIR = "out/java/cc&b/src/main/java/biz/doublegsoft/ccnb/service";


  @BeforeClass
  public static void initialize() throws Exception {
    new FileOutputStream(OUTPUT).close();
  }

  @Before
  public void test_gen_infos() throws Exception {
    ModelDefinition dataModel = loadModel("cc&b");
    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);

    ObjectDefinition customer = dataModel.findObjectByName("customer");
    ObjectDefinition customerInfo = projBuilder.build(customer);

    ObjectDefinition account = dataModel.findObjectByName("account");
    ObjectDefinition accountInfo = projBuilder.build(account);

    ObjectDefinition bill = dataModel.findObjectByName("bill");
    ObjectDefinition billInfo = projBuilder.build(bill);

    ObjectDefinition billSegment = dataModel.findObjectByName("bill_segment");
    ObjectDefinition billSegmentInfo = projBuilder.build(billSegment);

    ObjectDefinition serviceAgreement = dataModel.findObjectByName("service_agreement");
    ObjectDefinition serviceAgreementInfo = projBuilder.build(serviceAgreement);

    ObjectDefinition adjustment = dataModel.findObjectByName("adjustment");
    ObjectDefinition adjustmentInfo = projBuilder.build(adjustment);

    ObjectDefinition financialTransaction = dataModel.findObjectByName("financial_transaction");
    ObjectDefinition financialTransactionInfo = projBuilder.build(financialTransaction,
        new HashSet<>(Arrays.asList("bill", "account")));

    printUsecaseForModelbase(OUTPUT, null, accountInfo, customerInfo, billInfo,
        billSegmentInfo, financialTransactionInfo, serviceAgreementInfo, adjustmentInfo);
  }

  @Test
  public void test_get_bill() throws Exception {
    ModelDefinition dataModel = loadModel("cc&b");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@get_bill({bill: bill_id}):" +
            "{bill} <> {account} <> [bill_segment] <> [service_agreement] <> [financial_transaction]";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);

    AggregateBuilder builder = new AggregateBuilder(dataModel);
    AggregateRelationshipChain chain = builder.build(usecase.getReturnedObject());

    for (Relationship rel : chain.getRelationships()) {
      System.out.println(rel);
    }
    usecase.setOption("relations", chain);

    List<ObjectRelationships> objRelsList = chain.build();

    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);
    ObjectDefinition bill = dataModel.findObjectByName("bill");
    ObjectDefinition billInfo = projBuilder.build(bill);

    ObjectDefinition billSegment = dataModel.findObjectByName("bill_segment");
    ObjectDefinition billSegmentInfo = projBuilder.build(billSegment);

    ObjectDefinition serviceAgreement = dataModel.findObjectByName("service_agreement");
    ObjectDefinition serviceAgreementInfo = projBuilder.build(serviceAgreement);

    ObjectDefinition adjustment = dataModel.findObjectByName("adjustment");
    ObjectDefinition adjustmentInfo = projBuilder.build(adjustment);

    ObjectDefinition financialTransaction = dataModel.findObjectByName("financial_transaction");
    ObjectDefinition financialTransactionInfo = projBuilder.build(financialTransaction,
        new HashSet<>(Arrays.asList("bill", "account")));

    AssociationBuilder assocBuilder = new AssociationBuilder(dataModel);
    AssociationChain assocChain = assocBuilder.build(usecase.getParameterizedObject(),
        usecase.getReturnedObject());
    for (ObjectDefinition objInChain : assocChain.getAssociatingObjects()) {
      System.out.println(objInChain.getName());
    }

    printUsecaseForModelbase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

  @Test
  public void test_ft_list() throws Exception {
    ModelDefinition dataModel = loadModel("cc&b");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@find_ft({bill: bill_id}):" +
            "[{financial_transaction} <financial_transaction.ft_type_id = adjustment.id> {adjustment}]";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    ObjectDefinition paramObj = usecase.getParameterizedObject();
    ObjectDefinition retObj = usecase.getReturnedObject();

    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);
    ObjectDefinition ft = dataModel.findObjectByName("financial_transaction");
    ObjectDefinition ftInfo = projBuilder.build(ft);
    ftInfo.setName(retObj.getName().substring(1) + "_result");

    AssociationBuilder assocBuilder = new AssociationBuilder(dataModel);
    AssociationChain assocChain = assocBuilder.build(paramObj, retObj);
    for (ObjectDefinition objInChain : assocChain.getAssociatingObjects()) {
      System.out.println(objInChain.getName());
    }
    printUsecaseForModelbase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

  @Test
  public void test_get_account_by_premise() throws Exception {
    ModelDefinition dataModel = loadModel("cc&b");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@get_account_by_premise({premise: premise_id}):" +
            "{account} <> [{account_attribute}]";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    ObjectDefinition paramObj = usecase.getParameterizedObject();
    ObjectDefinition retObj = usecase.getReturnedObject();

    AssociationBuilder assocBuilder = new AssociationBuilder(dataModel);
    AssociationChain assocChain = assocBuilder.build(paramObj, retObj);
    for (ObjectDefinition objInChain : assocChain.getAssociatingObjects()) {
      System.out.println(objInChain.getName());
    }
    printUsecaseForModelbase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_ROOT + "/service/impl/$usecase$ServiceImpl.java.ftl", usecase, dataModel);
  }

  @Test
  public void test_get_account_by_bill_segment() throws Exception {
    ModelDefinition dataModel = loadModel("cc&b");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@get_account_by_bill_segment({bill_segment: id}):" +
            "{account} <> [{account_attribute}]";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    ObjectDefinition paramObj = usecase.getParameterizedObject();
    ObjectDefinition retObj = usecase.getReturnedObject();

    AssociationBuilder assocBuilder = new AssociationBuilder(dataModel);
    AssociationChain assocChain = assocBuilder.build(paramObj, retObj);
    for (ObjectDefinition objInChain : assocChain.getAssociatingObjects()) {
      System.out.println(objInChain.getName());
    }
    printUsecaseForModelbase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_ROOT + "/service/impl/$usecase$ServiceImpl.java.ftl", usecase, dataModel);
  }

  @Test
  public void test_generate_bill() throws Exception {
    ModelDefinition dataModel = loadModel("cc&b");
    String expr =
        "@generate_bill({account: account_id!}):{bill}\n" +
        "|&| account = {account}#(account_id)!'账户没有找到'\n" +
        "|&| service_agreements = [service_agreement]#(account_id)!'该账户没有任何服务协议'\n" +
        "|:| bill_segments = [{bill_segment: service_agreement = service_agreement_id, type = 'MR', " +
            "amount = %{meter_read: usage}#(premise_id = premise_id, status = 'E') * " +
            "{cfg_rate: rate}#(rate_id)%}]&service_agreements\n" +
        "|+| {bill: id = '', status = 'E'}\n" +
        "|+| [{bill_segment: status = 'E', bill = bill_id}]&bill_segments";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);

    AssignmentDefinition assign = (AssignmentDefinition) usecase.getStatements().get(0);
    Assert.assertEquals("第一个语句通过账户标识查找账户（对象值）",
        "account", assign.getValue().getObjectValue().getLabelledOption("original", "object"));
    assign = (AssignmentDefinition) usecase.getStatements().get(1);
    Assert.assertEquals("第二个语句通过账户标识查找服务协议（数组值）",
        "service_agreement", assign.getValue().getArrayValue().getLabelledOption("original", "object"));
    // 第三条语句，需要重点校验
    assign = (AssignmentDefinition) usecase.getStatements().get(2);
    Assert.assertEquals("第三个语句把所有查询出来的服务协议转换成账单项目",
        "bill_segments", assign.getAssignee());
    Assert.assertEquals("bill_segment", assign.getValue().getArrayValue().getLabelledOption("original", "object"));

    // 验证第三个语句中的计算表达式赋值
    ValuedAttributeDefinition attrAmount = (ValuedAttributeDefinition) assign.getValue().getArrayValue().getAttributes()[2];
    CalcExprDefinition calcExpr = attrAmount.getValue().getCalcExpr();
    List<ValueDefinition> operands = calcExpr.getOperands();
    Assert.assertEquals("表达式中的有两个对象变量", 2, operands.size());
    Assert.assertNotNull("表达式的被赋值变量是个数组", assign.getValue().getArrayValue());

    ValueDefinition firstOperand = calcExpr.getLeftOperand().getValue(); // operands.get(0);
    ValueDefinition secondOperand = calcExpr.getRightOperand().getValue(); // operands.get(1);
    AttributeDefinition firstAttrInApi = firstOperand.getObjectValue().getAttributes()[0];
    AttributeDefinition secondAttrInApi = secondOperand.getObjectValue().getAttributes()[0];
    Assert.assertEquals("第一个运算数的值", "usage", firstAttrInApi.getName());
    Assert.assertEquals("第一个运算数的值的原始数据对象", "meter_read",
        firstAttrInApi.getLabelledOption("original", "object"));
    Assert.assertEquals("第一个运算数的值的原始数据属性", "usage",
        firstAttrInApi.getLabelledOption("original", "attribute"));
    Assert.assertEquals("第二个运算数的值", "rate", secondAttrInApi.getName());
    Assert.assertEquals("第二个运算数的值的原始数据对象", "cfg_rate",
        secondAttrInApi.getLabelledOption("original", "object"));
    Assert.assertEquals("第二个运算数的值的原始数据属性", "rate",
        secondAttrInApi.getLabelledOption("original", "attribute"));

    List<String> uniqueObjNames = firstOperand.getObjectValue().getLabelledOptionAsList("unique", "object");
    List<String> uniqueAttrNames = firstOperand.getObjectValue().getLabelledOptionAsList("unique", "attribute");
    List<String> uniqueAttrTypes = firstOperand.getObjectValue().getLabelledOptionAsList("unique", "type");
    List<String> uniqueAttrVals = firstOperand.getObjectValue().getLabelledOptionAsList("unique", "value");
    Assert.assertEquals("第一个运算数的对象为空", "", uniqueObjNames.get(0));
    Assert.assertEquals("第一个运算数显式指明了目标（查找）对象的属性", "premise_id", uniqueAttrNames.get(0));
    Assert.assertEquals("第一个运算数显式指明了在等号之后的值", "premise_id", uniqueAttrVals.get(0));
    Assert.assertEquals("第一个运算数显式指明了在等号之后的值的类型", "premise_id", uniqueAttrTypes.get(0));
    Assert.assertEquals("第二个运算数的对象为空", "", uniqueObjNames.get(1));
    Assert.assertEquals("第二个运算数显式指明了目标（查找）对象的属性", "status", uniqueAttrNames.get(1));
    Assert.assertEquals("第二个运算数显式指明了在等号之后的值的类型", "string", uniqueAttrTypes.get(1));
    Assert.assertEquals("第二个运算数显式指明了在等号之后的值", "E", uniqueAttrVals.get(1));

    uniqueObjNames = secondOperand.getObjectValue().getLabelledOptionAsList("unique", "object");
    uniqueAttrNames = secondOperand.getObjectValue().getLabelledOptionAsList("unique", "attribute");
    uniqueAttrTypes = secondOperand.getObjectValue().getLabelledOptionAsList("unique", "type");
    uniqueAttrVals = secondOperand.getObjectValue().getLabelledOptionAsList("unique", "value");
    Assert.assertEquals("第二个运算数的对象为空，说明目标（查找）对象和参数对象有共同的属性", "", uniqueObjNames.get(0));
    Assert.assertEquals("即是目标对象的属性，也是查找对象的属性", "rate_id", uniqueAttrNames.get(0));
    Assert.assertEquals("域类型", "rate_id", uniqueAttrTypes.get(0));
    Assert.assertEquals("没有默认值", "", uniqueAttrVals.get(0));

    // 第三条语句校验结束
    SaveDefinition save = (SaveDefinition) usecase.getStatements().get(3);
    Assert.assertEquals("第四个语句通过新增一个账单数据",
        "bill", save.getSaveObject().getName());
    save = (SaveDefinition) usecase.getStatements().get(4);
    Assert.assertEquals("第五个语句通过新增许多账单明细数据",
        "bill_segment", save.getSaveObject().getName());
    Assert.assertTrue("账单明细数据是个数组", save.isArray());

    ObjectDefinition paramObj = usecase.getParameterizedObject();
    ObjectDefinition retObj = usecase.getReturnedObject();

    AssociationBuilder assocBuilder = new AssociationBuilder(dataModel);
    AssociationChain assocChain = assocBuilder.build(paramObj, retObj);
    for (ObjectDefinition objInChain : assocChain.getAssociatingObjects()) {
      System.out.println(objInChain.getName());
    }
    printUsecaseForModelbase(OUTPUT, usecase);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, OUTPUT_DIR + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, OUTPUT_DIR + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

}
