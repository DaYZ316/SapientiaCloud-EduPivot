package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Agent skill configuration loaded from yaml.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "agent.skills")
public class AgentSkillProperties {

    private ExternalSearch externalSearch = new ExternalSearch();
    private OpenTdb openTdb = new OpenTdb();

    @Data
    public static class ExternalSearch {

        /**
         * Master switch for all external-search based skills.
         */
        private boolean enabled = true;
    }

    @Data
    public static class OpenTdb {

        /**
         * Independent switch for the OpenTDB skill.
         */
        private boolean enabled = true;

        private String baseUrl = "https://opentdb.com";

        private String apiPath = "/api.php";

        private long connectTimeoutSeconds = 5L;

        private long requestTimeoutSeconds = 12L;
    }
}
