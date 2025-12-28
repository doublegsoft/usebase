package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.SpecBase;
import io.doublegsoft.usebase.Usebase;
import io.doublegsoft.usebase.projection.ProjectionBuilder;
import io.doublegsoft.usebase.relation.AggregateBuilder;
import io.doublegsoft.usebase.relation.AggregateRelations;
import io.doublegsoft.usebase.relation.Relationship;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CustomerCareAndBillingSpec extends SpecBase {

  @Test
  public void test_bill_root() throws Exception {
    ModelDefinition dataModel = loadModel("cc&b");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@get_bill({bill: bill_id}):" +
            "{bill} <> {account} <> [bill_segment] <> [service_agreement] <> [financial_transaction]";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    ObjectDefinition obj = usecase.getParameterizedObject();

    AggregateBuilder builder = new AggregateBuilder(dataModel, usecase);
    AggregateRelations rels = builder.build();
//    Assert.assertNotNull(rels.getRelation("user_role", "role"));
//    Assert.assertNotNull(rels.getRelation("role_permission", "role"));
//    Assert.assertNotNull(rels.getRelation("role_permission", "user_role"));
    for (Relationship rel : rels.getRelationships()) {
      System.out.println(rel);
    }
    usecase.setOption("relations", rels);

    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);
    ObjectDefinition bill = dataModel.findObjectByName("bill");
    ObjectDefinition billInfo = projBuilder.build(bill);

    ObjectDefinition billSegment = dataModel.findObjectByName("bill_segment");
    ObjectDefinition billSegmentInfo = projBuilder.build(billSegment);

    ObjectDefinition financialTransaction = dataModel.findObjectByName("financial_transaction");
    ObjectDefinition financialTransactionInfo = projBuilder.build(financialTransaction,
        new HashSet<>(Arrays.asList("bill", "account")));

    printUsecaseForModelbase(usecase, billSegmentInfo, financialTransactionInfo);
//    printJavaCodeForUsecase(TEMPLATE_ROOT + "/service/impl/$usercase$ServiceImpl.java.ftl", usecase, dataModel);
  }

}
