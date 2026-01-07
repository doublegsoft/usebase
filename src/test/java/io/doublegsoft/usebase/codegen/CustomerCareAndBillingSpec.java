package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.SpecBase;
import io.doublegsoft.usebase.Usebase;
import io.doublegsoft.usebase.association.AssociationBuilder;
import io.doublegsoft.usebase.association.AssociationChain;
import io.doublegsoft.usebase.projection.ProjectionBuilder;
import io.doublegsoft.usebase.aggregate.AggregateBuilder;
import io.doublegsoft.usebase.aggregate.AggregateRelationshipChain;
import io.doublegsoft.usebase.aggregate.ObjectRelationships;
import io.doublegsoft.usebase.aggregate.Relationship;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class CustomerCareAndBillingSpec extends SpecBase {

  @Test
  public void test_bill_aggregate() throws Exception {
    ModelDefinition dataModel = loadModel("cc&b");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@get_bill({bill: bill_id}):" +
            "{bill} <> {account} <> [bill_segment] <> [service_agreement] <> [financial_transaction]";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);

    AggregateBuilder builder = new AggregateBuilder(dataModel);
    AggregateRelationshipChain chain = builder.build(usecase.getReturnedObject());
//    Assert.assertNotNull(rels.getRelation("user_role", "role"));
//    Assert.assertNotNull(rels.getRelation("role_permission", "role"));
//    Assert.assertNotNull(rels.getRelation("role_permission", "user_role"));
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

    ObjectDefinition financialTransaction = dataModel.findObjectByName("financial_transaction");
    ObjectDefinition financialTransactionInfo = projBuilder.build(financialTransaction,
        new HashSet<>(Arrays.asList("bill", "account")));

    AssociationBuilder assocBuilder = new AssociationBuilder(dataModel);
    AssociationChain assocChain = assocBuilder.build(usecase.getParameterizedObject(),
        usecase.getReturnedObject());

    printUsecaseForModelbase(usecase, billInfo, billSegmentInfo, financialTransactionInfo);
    printJavaCodeForUsecase(TEMPLATE_ROOT + "/service/impl/$usercase$ServiceImpl.java.ftl", usecase, dataModel);
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

//    AggregateBuilder builder = new AggregateBuilder(dataModel);
//    AggregateRelationshipChain chain = builder.build(usecase.getReturnedObject());
//    List<ObjectRelationships> objRelsList = chain.build();

    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);
    ObjectDefinition ft = dataModel.findObjectByName("financial_transaction");
    ObjectDefinition ftInfo = projBuilder.build(ft);
    ftInfo.setName(retObj.getName().substring(1) + "_result");

    AssociationBuilder assocBuilder = new AssociationBuilder(dataModel);
    AssociationChain assocChain = assocBuilder.build(paramObj, retObj);

    printUsecaseForModelbase(usecase, ftInfo);
    printJavaCodeForUsecase(TEMPLATE_ROOT + "/service/impl/$usercase$ServiceImpl.java.ftl", usecase, dataModel);
  }

}
