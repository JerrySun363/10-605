import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapreduce.Job;

public class Aggregate {
	public static long bxCount = 0;
	public static long cxCount = 0;
	public static long bxVol = 0;
	public static long cxVol = 0;

	public static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, Text> {
		private Text word = new Text();
		private Text count = new Text();
		private static String stopwords = "i,the,to,and,a,an,of,it,you,that,in,my,is,was,for";
		private boolean isBigram = false;
		private static HashSet<String> words = new HashSet<String>();
		static {
			String[] mywords = stopwords.split(",");
			for(String word:mywords){
				words.add(word);
			}
		}

		public void configure(JobConf job) {
			isBigram = job.get("type").equals("1");
		}

		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> output, Reporter arg3)
				throws IOException {
			String line = value.toString();
			String[] tokens = line.split("\t");
			if (isBigram) {
				String[] mytokens = tokens[0].split(" ");
				if (words.contains(mytokens[0]) || words.contains(mytokens[1])) {
					return;
				}

			} else {
				if (words.contains(tokens[0])) {
					return;
				}
			}
			int year = Integer.parseInt(tokens[1]);
			int num = Integer.parseInt(tokens[2]);
			word.set(tokens[0]);
			if (year > 1960) {
				count.set("bx=" + num);
			} else {
				count.set("cx=" + num);
			}
			output.collect(word, count);
		}
	}

	public static class Reduce extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {

		public static enum MyCounters {
			NUM_BACK, NUM_FORE, BACK_VOL, FORE_VOL
		};

		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			int bxsum = 0;
			int cxsum = 0;
			while (values.hasNext()) {
				String line = values.next().toString();
				String[] tokens = line.split("=");
				if (tokens[0].equals("bx")) {
					bxsum += Integer.parseInt(tokens[1]);

				} else {
					cxsum += Integer.parseInt(tokens[1]);

				}
			}
			output.collect(key, new Text(bxsum + "\t" + cxsum));
			reporter.incrCounter(MyCounters.NUM_BACK, bxsum);
			reporter.incrCounter(MyCounters.NUM_FORE, cxsum);
			reporter.incrCounter(MyCounters.BACK_VOL, bxsum > 0 ? 1 : 0);
			reporter.incrCounter(MyCounters.FORE_VOL, cxsum > 0 ? 1 : 0);

		}
	}

	public static void run(String input, String output, String type)
			throws ClassNotFoundException, IOException, InterruptedException {
		JobConf conf = new JobConf(Aggregate.class);
		conf.setJobName("Aggregate");
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Map.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);
		conf.set("type", type);

		FileInputFormat.setInputPaths(conf, new Path(input));
		FileOutputFormat.setOutputPath(conf, new Path(output));

		// JobClient.runJob(conf);
		Job job = new Job(conf);

		job.waitForCompletion(true);

		bxCount = job.getCounters()
				.findCounter(Aggregate.Reduce.MyCounters.NUM_BACK).getValue();
		cxCount = job.getCounters()
				.findCounter(Aggregate.Reduce.MyCounters.NUM_FORE).getValue();
		bxVol = job.getCounters()
				.findCounter(Aggregate.Reduce.MyCounters.BACK_VOL).getValue();
		cxVol = job.getCounters()
				.findCounter(Aggregate.Reduce.MyCounters.FORE_VOL).getValue();

	}

}