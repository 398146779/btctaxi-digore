package com.btctaxi.gate.config;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@Component
public class EmailTemplate {
    private Map<String, Map<String, String>> templates = new HashMap<>();

    public EmailTemplate(DistConfig distConfig) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:/i18n/email/" + distConfig.getName() + "/content/*");

        for (Resource res : resources) {
            String fileName = res.getFilename();
            String name, locale;
            if (!fileName.contains("@")) {
                name = fileName.substring(0, fileName.lastIndexOf("."));
                locale = "";
            } else {
                String[] parts = fileName.split("@");
                name = parts[0];
                locale = parts[1].substring(0, parts[1].lastIndexOf("."));
            }
            String content = read(res);
            templates.computeIfAbsent(name, v -> new HashMap<>()).put(locale, content);
        }
    }

    public String format(String name, String locale, Object... params) {
        Map<String, String> locales = templates.get(name);
        if (locales == null)
            return null;
        String template = locales.get(locale == null ? "" : locale);
        if (template == null)
            template = locales.get("");
        if (template == null)
            return null;
        return MessageFormat.format(template, params);
    }

    private String read(Resource res) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(res.getInputStream(), "UTF-8"))) {
            String s;
            while ((s = reader.readLine()) != null)
                sb.append(s);
        } catch (Throwable e) {
        }
        return sb.toString();
    }
}
