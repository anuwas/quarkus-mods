package org.acme.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.scheduler.SchedulerClient;

import java.net.URI;
import java.util.Optional;

@ApplicationScoped
public class AwsSchedulerConfig {

    @ConfigProperty(name = "aws.access-key-id")
    String accessKeyId;

    @ConfigProperty(name = "aws.secret-access-key")
    String secretAccessKey;

    @ConfigProperty(name = "aws.region")
    String region;

    @ConfigProperty(name = "aws.scheduler.endpoint-override")
    Optional<String> endpointOverride;

    @Produces
    @ApplicationScoped
    public SchedulerClient schedulerClient() {
        var builder = SchedulerClient.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                        )
                )
                .httpClient(UrlConnectionHttpClient.builder().build());

        endpointOverride.ifPresent(endpoint -> builder.endpointOverride(URI.create(endpoint)));

        return builder.build();
    }
}

