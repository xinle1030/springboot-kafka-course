package net.javaguides.springboot;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@Service
public class WikimediaChangesProducer {

    @Value("${spring.kafka.topic.name}")
    private String topicName;

    private static final Logger LOGGER = LoggerFactory.getLogger(WikimediaChangesProducer.class);

    private KafkaTemplate<String, String> kafkaTemplate;

    public WikimediaChangesProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage() throws InterruptedException {
        // to read real time stream data from wikimedia, we use event source
        // need libraries: okHttp-eventsource, okhttp (to create event source and read
        // real time stream data),
        // jackson-core, jackson-databind (to deal with JSON data)

        // need a separate event handler class to handle the event
        EventHandler eventHandler = new WikimediaChangesHandler(kafkaTemplate, topicName);

        // create event source from the event handler to connect to the source url
        // EventSource.Builder is used to configure it with the eventHandler and the URI.
        // event source get all the real time stream data from source, and trigger the respective handler method
        String url = "https://stream.wikimedia.org/v2/stream/recentchange";
        EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(url));
        EventSource eventSource = builder.build();

        // starts the EventSource, initiating the connection to the Wikimedia API stream
        eventSource.start();

        TimeUnit.MINUTES.sleep(10); // sleep for 10min
    }
}
