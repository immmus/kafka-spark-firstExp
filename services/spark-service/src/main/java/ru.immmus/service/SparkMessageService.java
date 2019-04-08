package ru.immmus.service;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.stereotype.Service;
import ru.immmus.AggregateMessages;
import ru.immmus.Message;
import ru.immmus.MessageService;
import ru.immmus.Messages;
import ru.immmus.profiles.Spark;

import java.util.List;
import java.util.Map;

@Spark
@Service("sparkRDDService")
public class SparkMessageService implements MessageService {
    private final JavaSparkContext sc;

    public SparkMessageService(JavaSparkContext sc) {
        this.sc = sc;
    }

    @Override
    public AggregateMessages aggregate(Messages messages) {
        List<Message> messageList = messages.getMessages();
        JavaRDD<Message> msg = sc.parallelize(messageList);
        Map<String, Iterable<Message>> stringIterableMap = msg.groupBy(Message::getIdLocation).collectAsMap();
        long withBreakdowns = msg.map(Message::getIdDetected)
                .filter("Nan"::equals)
                .count();
        return AggregateMessages.builder()
                .countEventsWithBreakdowns(withBreakdowns)
                .countEventsWithoutBreakdowns(messageList.size() - withBreakdowns)
                .aggregateMap(stringIterableMap)
                .build();
    }
}
