package com.artezio.arttime.repositories;

import org.apache.commons.io.FilenameUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Named
@Stateless
public class ReportTemplateRepository {

    private static String TEMPLATE_FILES_RESOURCE_PATH = "/WEB-INF/classes/report_templates/";
    @Inject
    private ServletContext context;

    public List<String> getNames() {
        return getTemplateFilePaths().stream()
                .map(FilenameUtils::getBaseName)
                .collect(Collectors.toList());
    }

    public InputStream getTemplate(String templateName) {
        return context.getResourceAsStream(TEMPLATE_FILES_RESOURCE_PATH + templateName + ".rptdesign");
    }

    private Collection<String> getTemplateFilePaths() {
        return context.getResourcePaths(TEMPLATE_FILES_RESOURCE_PATH);
    }

}
