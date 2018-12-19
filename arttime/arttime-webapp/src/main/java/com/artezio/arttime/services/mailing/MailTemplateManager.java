package com.artezio.arttime.services.mailing;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.ejb.Stateless;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * The Class TemplateManager.
 */
@Stateless
public class MailTemplateManager {

    /**
     * Gets the template text.
     *
     * @param templateFileName the template file name
     * @param dataModel the data model
     * @return the template text
     */
    public String getTemplateText(String templateFileName, @SuppressWarnings("rawtypes") Map dataModel) {
        try {
            Template template = getConfiguration().getTemplate(templateFileName);
            Writer out = new StringWriter();
            template.process(dataModel, out);
            return out.toString();
        } catch (TemplateException e) {
            throw new RuntimeException("Some problems occured while executing the template!", e);
        } catch (IOException e) {
            throw new RuntimeException("I/O problem occured with template file!", e);
        }
    }

    protected Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading(MailTemplateManager.class, "/");
        configuration.setObjectWrapper(new DefaultObjectWrapper());
        return configuration;
    }

    public String getTemplateText(String templateFileName) {
        return getTemplateText(templateFileName, null);
    }

}
