package es.accenture.flink.Job;

import es.accenture.flink.Sink.KuduSink;
import es.accenture.flink.Utils.RowSerializable;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.util.Properties;
import java.util.UUID;

/**
 * Job which reads from kafka, make some changes, and writes the new data into a Kudu database
 */
public class JobStreamingInputOutput {

    public static void main(String[] args) throws Exception {

        //********Only for test, delete once finished*******
        args[0] = "KafkaToTable" /*args[0]*/;
        args[1] = "test" /*args[1]*/;
        args[2] = "localhost" /*args[2]*/;
        //**************************************************


        if(args.length!=3){
            System.out.println( "JobStreamingInputOutput params: [TableToWrite] [Topic] [Master Address]\n");
            return;
        }


        String tableName = args[0] /*args[0]*/;
        String topic = args[1] /*args[1]*/;
        String KUDU_MASTER = args[2] /*args[2]*/;
        String [] columnNames = new String[2];
        columnNames[0] = "key";
        columnNames[1] = "value";

        UUID id = UUID.randomUUID();

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", "localhost:9092");
        prop.setProperty("group.id", String.valueOf(id));
        prop.setProperty("auto.offset.reset", "latest");
        prop.setProperty("zookeeper.connect", "localhost:2181");
        prop.setProperty("topic", topic);

        DataStream<String> stream = env.addSource(new FlinkKafkaConsumer09<>(
                prop.getProperty("topic"),
                new SimpleStringSchema(),
                prop));

        DataStream<RowSerializable> stream2 = stream.map(new MyMapFunction());

        stream2.addSink(new KuduSink(KUDU_MASTER, tableName, columnNames));

        env.execute();
    }

    private static class MyMapFunction implements MapFunction<String, RowSerializable>{

        @Override
        public RowSerializable map(String input) throws Exception {

            RowSerializable res = new RowSerializable(2);
            Integer i = 0;
            for (String s : input.split(" ")) {
                res.setField(i, s);
                i++;
            }
            return res;
        }
    }




}






