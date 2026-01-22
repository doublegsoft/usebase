package io.doublegsoft.usebase.codegen;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.SpecBase;
import io.doublegsoft.usebase.Usebase;
import io.doublegsoft.usebase.aggregate.AggregateBuilder;
import io.doublegsoft.usebase.aggregate.AggregateRelationshipChain;
import io.doublegsoft.usebase.association.AssociationBuilder;
import io.doublegsoft.usebase.association.AssociationChain;
import io.doublegsoft.usebase.projection.ProjectionBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class AbpmsSpec extends SpecBase {

  @Test
  public void test_get_bill_aggregate() throws Exception {
    ModelDefinition dataModel = loadModel("abpms");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@get_bill_aggregate({bill: bill_id}):" +
            "{bill: bill_id, comp_date} <bill.account_id = account.account_id> " +
            "{account: account_id} <> [bitem] <> [svc_dtl] <> [fin_tran]";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    checkOriginalIndexAndObject(usecase.getReturnedObject());

    AssociationBuilder assocBuilder = new AssociationBuilder(dataModel);
    AssociationChain assocChain = assocBuilder.build(usecase.getParameterizedObject(), usecase.getReturnedObject());

    AggregateBuilder builder = new AggregateBuilder(dataModel);
    AggregateRelationshipChain chain = builder.build(usecase.getReturnedObject());

    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);
  }

  @Test
  public void test_find_bitems() throws Exception {
    ModelDefinition dataModel = loadModel("abpms");
    ModelDefinition apiModel = new ModelDefinition();
    String expr =
        "@find_bitems({bill: bill_id}):[" +
        "  {bitem: bitem_id, desc_on_bill} <bill_id> " +
        "  {bill: bill_id, comp_date} <svc_id> " +
        "  {svc_dtl: svc_id, svc_type_code} <svc_type_code + obs_sw = 'N'>" +
        "  {cfg_svc_type: svc_type_dflt_desc_on_bill_tc}" +
        "]";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    checkOriginalIndexAndObject(usecase.getReturnedObject());

    for (AttributeDefinition attr : usecase.getReturnedObject().getAttributes()) {
      String sourceObject = attr.getLabelledOption("conjunction", "source_object");
      String targetObject = attr.getLabelledOption("conjunction", "target_object");
      String sourceAttribute = attr.getLabelledOption("conjunction", "source_attribute");
      String targetAttribute = attr.getLabelledOption("conjunction", "target_attribute");
      System.out.println(attr.getName() + "  " + attr.getLabelledOption("original", "index"));
      System.out.println("  " + sourceObject + "(" + sourceAttribute + ") <> " +targetObject + "(" +
          targetAttribute + ")");
    }
    AssociationBuilder assocBuilder = new AssociationBuilder(dataModel);
    AssociationChain assocChain = assocBuilder.build(usecase.getParameterizedObject(), usecase.getReturnedObject());

    ProjectionBuilder projBuilder = new ProjectionBuilder(dataModel);
    ObjectDefinition bitem = dataModel.findObjectByName("bitem");
    ObjectDefinition bitemInfo = projBuilder.build(bitem, Arrays.asList(usecase.getReturnedObject().getAttributes()));
    Assert.assertEquals("属性应该有七个", 7, usecase.getReturnedObject().getAttributes().length);
  }

}
