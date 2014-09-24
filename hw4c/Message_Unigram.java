import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapreduce.Job;


public class Message_Unigram {
	public static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, Text> {
		private Text word = new Text();
		private Text count = new Text();

		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> output, Reporter arg3)
				throws IOException {
			String line = value.toString();
			String[] tokens = line.split("\t");
			if (tokens[0].contains(" ")) {
				String[] subtokens = tokens[0].split(" ");
				word.set(subtokens[0]);
				count.set(tokens[0]);
				output.collect(word, count);
				
				word.set(subtokens[1]);
				output.collect(word, count);
			} else {
				word.set(tokens[0]);
				count.set(tokens[1] + "\t" + tokens[2]);
				output.collect(word, count);
			}
		}
	}

	public static class Reduce extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {
		private Text word = new Text();
		private Text count = new Text();

		@Override
		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter arg3)
				throws IOException {
			//int bx = 0;
			int cx = 0;
			ArrayList<String> list = new ArrayList<String>();

			while (values.hasNext()) {
				String line = values.next().toString();
				String[] tokens = line.split("\t");
				if (tokens.length == 2) {
					//bx = Integer.parseInt(tokens[0]);
					cx = Integer.parseInt(tokens[1]);
				} else {
					list.add(tokens[0]);
				}
			}
			Iterator<String> iterator = list.iterator();
			while (iterator.hasNext()) {
				String s = iterator.next();
				String[] tokens = s.split(" ");
				word.set(s);
				if (key.toString().equals(tokens[0])) {
					count.set("cx=" + cx);
				} else {
					count.set("cy=" + cx);
				}
				output.collect(word, count);
			}

		}
	}

	public static void run(String input1, String input2, String output) throws ClassNotFoundException,
			IOException, InterruptedException {
		JobConf conf = new JobConf(Message_Unigram.class);
		conf.setJobName("Message_Unigram");
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Map.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);
		
		
		FileInputFormat.setInputPaths(conf, new Path(input1), new Path(input2));
		FileOutputFormat.setOutputPath(conf, new Path(output));

		//JobClient.runJob(conf);
		Job job = new Job(conf);
		job.waitForCompletion(true);
		

	}

}
