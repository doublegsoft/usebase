package io.doublegsoft.usebase.relation;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.usebase.SpecBase;
import io.doublegsoft.usebase.Usebase;
import org.junit.Assert;
import org.junit.Test;

public class AggregateBuilderTest extends SpecBase {

  @Test
  public void test_iam_get_user() throws Exception {
    ModelDefinition dataModel = loadModel("iam");
    String expr =
        "@get_user({user: user_id, (username, email)}):" +
            "{user: user_id, username, email} <user_role> [role] <role_permission> [permission]";
    UsecaseDefinition usecase = new Usebase(dataModel).parse(expr).get(0);
    AggregateBuilder builder = new AggregateBuilder(dataModel, usecase);
    AggregateRelations rels = builder.build();
    Assert.assertNotNull(rels.getRelation("user_role", "role"));
    Assert.assertNotNull(rels.getRelation("role_permission", "role"));
    Assert.assertNotNull(rels.getRelation("role_permission", "user_role"));
    System.out.println(rels);
  }

}
