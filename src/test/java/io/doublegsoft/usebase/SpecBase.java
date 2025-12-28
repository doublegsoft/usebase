package io.doublegsoft.usebase;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.ReturnedObjectDefinition;
import com.doublegsoft.jcommons.metamodel.StatementDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.modelbase.Modelbase;
import io.doublegsoft.usebase.modelbase.ModelbaseWriter;
import io.doublegsoft.usebase.output.TemplateOutputWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpecBase {

  public static final String TEMPLATE_ROOT = "java-tx@spring-1.x/src/main/java/$namespace$/$app$";

  protected ModelDefinition loadModel(String... projs) throws Exception {
    String content = "";
    for (String proj : projs) {
      InputStream input = SpecBase.class.getResourceAsStream("/model/" + proj + ".modelbase");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buff = new byte[4096];
      int len = 0;
      while ((len = input.read(buff)) > 0) {
        baos.write(buff, 0, len);
      }
      baos.flush();
      input.close();

      content += new String(baos.toByteArray(), "UTF-8");
    }
    return new Modelbase().parse(content);
  }

  protected void printStatements(List<StatementDefinition> stmts) {
    for (StatementDefinition stmt : stmts) {
      System.out.println(stmt.getOperator());
      printStatements(stmt.getStatements());
    }
  }

  protected void printUsecaseForModelbase(UsecaseDefinition usecase, ObjectDefinition... objs) throws IOException {
    StringWriter sw = new StringWriter();
    ModelbaseWriter writer = new ModelbaseWriter(sw);
    writer.write(usecase.getParameterizedObject());
    writer.write(usecase.getReturnedObject());
    if (objs != null && objs.length > 0) {
      for (ObjectDefinition obj : objs) {
        writer.write(obj);
      }
    }
    System.out.println(sw);
  }

  protected void printJavaCodeForUsecase(String templateName, UsecaseDefinition usecase, ModelDefinition dataModel) throws IOException {
    StringWriter sw = new StringWriter();
    Map<String,Object> app = new HashMap<>();
    app.put("name", "test");
    Map<String,Object> data = new HashMap<>();
    TemplateOutputWriter writer = new TemplateOutputWriter(sw,
        "../usebase-data",
        "../usebase-data/java");
    data.put("namespace", "hello.world");
    data.put("app", app);
    data.put("model", dataModel);
    writer.write(templateName, usecase, data);
    System.out.println(sw);
  }

}
