package dev.jab125.metahelper.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({"groupId", "artifactId"})
public class Metadata {
    public Versioning versioning;

    @JsonIgnoreProperties({"latest", "release", "lastUpdated"})
    public static class Versioning {
        @JacksonXmlElementWrapper(namespace = "version")
        public List<String> versions = new ArrayList<>();
    }
}
