import java.io.IOException;
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

public class Compute {

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
			word.set(tokens[0]);
			if (tokens.length == 3) {
				count.set(tokens[1] + "\t" + tokens[2]);
			} else {
				count.set(tokens[1]);
			}
			output.collect(word, count);
		}

	}

	public static class Reduce extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {
		private Text word = new Text();
		private Text count = new Text();

		private long cxytotal = 0;
		private long bxytotal = 0;
		private long ctotal = 0;
		
		private long cxyVol = 0;
		private long bxyVol = 0;
		private long cVol = 0;
		

		public void configure(JobConf job) {
			cxytotal = Long.parseLong(job.get("cxytotal"));
			bxytotal = Long.parseLong(job.get("bxytotal"));
			ctotal = Long.parseLong(job.get("ctotal"));
            
			cxyVol = Long.parseLong(job.get("cxyVol"));
			cVol = Long.parseLong(job.get("cVol"));
			bxyVol = Long.parseLong(job.get("bxyVol"));
			
		}

		@Override
		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter arg3)
				throws IOException {
			int cxy = 0, bxy = 0, cx = 0, cy = 0;

			while (values.hasNext()) {
				String line = values.next().toString();
				String[] tokens = line.split("\t");
				if (tokens.length == 2) {
					bxy = Integer.parseInt(tokens[0]);
					cxy = Integer.parseInt(tokens[1]);
				} else {
					String[] subtokens = line.split("=");
					if (subtokens[0].equals("cx")) {
						cx = Integer.parseInt(subtokens[1]);
					} else {
						cy = Integer.parseInt(subtokens[1]);
					}
				}

			}
			double p = (1 + cxy) * 1.0 / (cxytotal);
			double q = (1 + cx) * 1.0 / (ctotal) * (1 + cy) / (ctotal);
			double phraseScore = p * Math.log(p / q);
			q = (1 + bxy) * 1.0 / (bxytotal);
			double infoScore = p * Math.log(p / q);
			double totalScore = infoScore + phraseScore;

			count.set(totalScore + " " + phraseScore + " " + infoScore);
			output.collect(key, count);
		}
	}

	public static void run(String input1, String input2, String output,
			long cxytotal, long bxytotal, long ctotal, long cxyVol, long bxyVol, long cVol)
			throws ClassNotFoundException, IOException, InterruptedException {
		JobConf conf = new JobConf(Compute.class);
		conf.setJobName("Compute");
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Map.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		conf.set("cxytotal", cxytotal + "");
		conf.set("bxytotal", bxytotal + "");
		conf.set("ctotal", ctotal + "");
		conf.set("cxyVol", cxyVol+"");
		conf.set("bxyVol", bxyVol+"");
		conf.set("cVol", cVol+"");

		FileInputFormat.setInputPaths(conf, new Path(input1), new Path(input2));
		FileOutputFormat.setOutputPath(conf, new Path(output));

		// JobClient.runJob(conf);
		Job job = new Job(conf);
		job.waitForCompletion(true);

	}

}
