package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.ScheduleResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.scheduler.SchedulerClient;
import software.amazon.awssdk.services.scheduler.model.*;

@ApplicationScoped
public class EventBridgeScheduleService {

    private static final Logger LOG = Logger.getLogger(EventBridgeScheduleService.class);

    @Inject
    SchedulerClient schedulerClient;

    @ConfigProperty(name = "app.scheduler.event-bus-arn")
    String eventBusArn;

    @ConfigProperty(name = "app.scheduler.role-arn")
    String roleArn;

    /**
     * Creates an EventBridge Scheduler schedule that will send an event to the
     * default EventBridge event bus at the specified time.
     * <p>
     * An EventBridge Rule ("scheduler-to-api-destination") on the default bus
     * matches events with source "custom.scheduler" and detail-type "ScheduledEvent",
     * and routes them to the API Destination which invokes the HTTP endpoint.
     * <p>
     * Architecture: Scheduler → Event Bus → Rule → API Destination → HTTP endpoint
     *
     * @param request the request containing schedule name and time
     * @return response with schedule details
     */
    public ScheduleResponse createSchedule(org.acme.dto.CreateScheduleRequest request) {
        LOG.infof("Creating schedule '%s' at time '%s'", request.getScheduleName(), request.getScheduleTime());

        // Build the schedule expression: "at(yyyy-MM-ddTHH:mm:ss)" for one-time schedule
        String scheduleExpression = "at(" + request.getScheduleTime() + ")";

        // Build the payload that will be sent as the event detail
        String targetInput = String.format(
                "{\"scheduleName\": \"%s\", \"scheduledTime\": \"%s\"}",
                request.getScheduleName(),
                request.getScheduleTime()
        );

        try {
            // Target the default EventBridge event bus.
            // EventBridgeParameters sets the Source and DetailType for the event,
            // which the EventBridge Rule uses to match and route to the API Destination.
            CreateScheduleResponse response = schedulerClient.createSchedule(CreateScheduleRequest.builder()
                    .name(request.getScheduleName())
                    .scheduleExpression(scheduleExpression)
                    .scheduleExpressionTimezone("UTC")
                    .flexibleTimeWindow(FlexibleTimeWindow.builder()
                            .mode(FlexibleTimeWindowMode.OFF)
                            .build())
                    .target(Target.builder()
                            .arn(eventBusArn)
                            .roleArn(roleArn)
                            .input(targetInput)
                            .eventBridgeParameters(EventBridgeParameters.builder()
                                    .detailType("ScheduledEvent")
                                    .source("custom.scheduler")
                                    .build())
                            .retryPolicy(RetryPolicy.builder()
                                    .maximumRetryAttempts(2)
                                    .maximumEventAgeInSeconds(3600)
                                    .build())
                            .build())
                    .state(ScheduleState.ENABLED)
                    .description("Schedule created via REST API for: " + request.getScheduleName())
                    .actionAfterCompletion(ActionAfterCompletion.DELETE)
                    .build());

            LOG.infof("Schedule created successfully with ARN: %s", response.scheduleArn());

            return new ScheduleResponse(
                    "Schedule created successfully",
                    request.getScheduleName(),
                    response.scheduleArn()
            );

        } catch (ConflictException e) {
            LOG.errorf("Schedule with name '%s' already exists: %s", request.getScheduleName(), e.getMessage());
            throw e;
        } catch (SchedulerException e) {
            LOG.errorf("Failed to create schedule: %s", e.getMessage());
            throw e;
        }
    }
}
