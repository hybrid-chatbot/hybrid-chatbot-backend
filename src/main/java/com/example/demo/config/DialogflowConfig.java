package com.example.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class DialogflowConfig {

    @Value("${dialogflow.project-id}")
    private String projectId;

    @Bean
    public SessionsClient sessionsClient() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ClassPathResource("credentials/dialogflow-service-account.json").getInputStream()
        );

        SessionsSettings settings = SessionsSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

        return SessionsClient.create(settings);
    }

    @Bean
    public String projectId() {
        return projectId;
    }
}