package io.doublegsoft.usebase;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metamodel.StatementDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.modelbase.Modelbase;
import io.doublegsoft.usebase.modelbase.ModelbaseWriter;
import io.doublegsoft.usebase.output.TemplateOutputWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

public class SpecBase {

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

  protected void printUsecaseForModelbase(UsecaseDefinition usecase) throws IOException {
    StringWriter sw = new StringWriter();
    ModelbaseWriter writer = new ModelbaseWriter(sw);
    writer.write(usecase.getParameterizedObject());
    writer.write(usecase.getReturnedObject());
    System.out.println(sw);
  }

  protected void printJavaModelForUsecase(String templateName, UsecaseDefinition usecase) throws IOException {
    StringWriter sw = new StringWriter();
    TemplateOutputWriter writer = new TemplateOutputWriter(sw,
        "src/test/resources/template/java-model");
    writer.write(templateName, usecase);
    System.out.println(sw);
  }

}
