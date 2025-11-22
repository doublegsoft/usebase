package io.doublegsoft.usebase.output;

import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import com.doublegsoft.jcommons.programming.java.JavaNamingConvention;
import com.doublegsoft.jcommons.utils.Inflector;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.doublegsoft.typebase.Typebase;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateOutputWriter {

  private static final Configuration FREEMARKER = new Configuration(Configuration.VERSION_2_3_28);

  private final List<FileTemplateLoader> loaders = new ArrayList<>();

  private final Writer writer;

  public TemplateOutputWriter(Writer writer, String... roots) throws IOException {
    this.writer = writer;
    if (roots == null || roots[0] == null) {
      throw new IllegalArgumentException("the given roots is null");
    }
    for (String r : roots) {
      loaders.add(new FileTemplateLoader(new File(r)));
    }
  }

  public TemplateOutputWriter write(String templateName, UsecaseDefinition usecase) throws IOException {
    return write(templateName, usecase, new HashMap<>());
  }

  public TemplateOutputWriter write(String templateName, UsecaseDefinition usecase, Map<String,Object> others) throws IOException {
    MultiTemplateLoader templateLoader = new MultiTemplateLoader(loaders.toArray(new TemplateLoader[0]));
    FREEMARKER.setTemplateLoader(templateLoader);

    Map<String, Object> data = new HashMap<>();
    data.put("usecase", usecase);
    data.put("java", new JavaNamingConvention());
    data.put("inflector", Inflector.getInstance());
    data.put("typebase", new Typebase());
    data.putAll(others);
    Template tpl = FREEMARKER.getTemplate(templateName);
    try {
      tpl.process(data, writer);
    } catch (TemplateException ex) {
      throw new IOException(ex);
    }
    return this;
  }


}
