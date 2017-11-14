package com.trustev;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PropertyHelper {

    private Properties prop;
    private String propertiesFilename;

    public PropertyHelper(String filename) throws IOException, URISyntaxException {
        prop = new Properties();
        propertiesFilename = filename;
        loadProperties();
    }

    private Properties loadProperties() throws IOException, URISyntaxException {
        InputStream input = null;

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Path path = Paths.get(classLoader.getResource(propertiesFilename).toURI());
        input = new FileInputStream(path.toString());
        prop.load(input);
        return prop;
    }

    public List<String> loadProperty(String property) {
        String value = prop.getProperty(property);
        List<String> topicsList = Arrays.asList(value.split("\\s*,\\s*"));

        return topicsList;
    }

    public String reconnectString(List<String> toConnect){
        StringBuilder builder = new StringBuilder();
        int size = toConnect.size();
        for (int i=0; i<size; i++){
            builder.append(toConnect.get(i));
            if (i<size-1){
                builder.append(",");
            }
        }

        return builder.toString();
    }

}
