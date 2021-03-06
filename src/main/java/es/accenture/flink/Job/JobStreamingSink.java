package es.accenture.flink.Job;

import es.accenture.flink.Sink.KuduSink;
import es.accenture.flink.Utils.RowSerializable;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.OutputStream;

/**
 * A job which reads a line of elements, split the line by spaces to generate the rows,
 * and writes the result on another Kudu database using streaming functions.
 * (This example split the line by spaces)
 */
public class JobStreamingSink {

    public static void main(String[] args) throws Exception {

        /********Only for test, delete once finished*******/
        args[0] = "TableStreamingSink";
        args[1] = "localhost";
        /**************************************************/

        if(args.length!=2){
            System.out.println( "JobStreamingSink params: [TableToWrite] [Master Address]\n");
            return;
        }

        // Params of program
        String tableName = args[0];
        String KUDU_MASTER = args[1];

        // Schema of the table to create
        String [] columnNames = new String[3];
        columnNames[0] = "col1";
        columnNames[1] = "col2";
        columnNames[2] = "col3";

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<String> stream = env.fromElements("field1 field2 field3");

        DataStream<RowSerializable> stream2 = stream.map(new MyMapFunction());

        stream2.addSink(new KuduSink(KUDU_MASTER, tableName, columnNames));

        env.execute();
    }

    /**
     * Map function which receives a String, splits it, and creates as many row as word has the string
     * This row contains two fields, first field is a serial generated automatically starting in 0,
     * second field is the substring generated by the split function.
     */
    private static class MyMapFunction implements MapFunction<String, RowSerializable>{

        @Override
        public RowSerializable map(String input) throws Exception {

            RowSerializable res = new RowSerializable(3);
            Integer i = 0;
            for (String s : input.split(" ")) {
                /*Needed to prevent exception on map function if phrase has more than 4 words*/
                if(i<3) res.setField(i, s);
                i++;
            }
            return res;
        }
    }
}