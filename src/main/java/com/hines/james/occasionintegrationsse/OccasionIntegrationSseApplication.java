package com.hines.james.occasionintegrationsse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

interface MessageChannels {
    @Input
    SubscribableChannel input();
}

@EnableBinding(MessageChannels.class)
@SpringBootApplication
@RestController
public class OccasionIntegrationSseApplication {

    @Bean
    IntegrationFlow greetingsFlow(MessageChannels channels) {
        return IntegrationFlows.from(channels.input())
                .handle(String.class, (payload, headers) -> {
                    System.out.println(payload);
                    return null;
                })
                .get();
    }

	@GetMapping(value = "/files/{name}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> files(@PathVariable String name, MessageChannels channels) {
        return Flux.<String>create( emitter -> {
            MessageHandler handler = msg -> emitter.next(String.class.cast(msg.getPayload()));

            emitter.onDispose(() -> channels.input().unsubscribe(handler));
            channels.input().subscribe(handler);
        });
    }

	public static void main(String[] args) {
		SpringApplication.run(OccasionIntegrationSseApplication.class, args);
	}
}
