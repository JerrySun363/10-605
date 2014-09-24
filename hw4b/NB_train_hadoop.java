import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

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

public class NB_train_hadoop {
	public static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, IntWritable> {
		private Text word = new Text();
		private IntWritable count = new IntWritable(0);

		public Vector<String> tokenizeDoc(String cur_doc) {
			String[] words = cur_doc.split("\\s+");
			Vector<String> tokens = new Vector<String>();
			for (int i = 0; i < words.length; i++) {
				words[i] = words[i].replaceAll("\\W", "");
				if (words[i].length() > 0) {
					tokens.add(words[i]);
				}
			}
			return tokens;
		}

		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<Text, IntWritable> output, Reporter arg3)
				throws IOException {
			String line = value.toString();
			String[] parts = line.split("\t");
			String[] labels = parts[0].split(",");
			String cur_doc = parts[1];
			Vector<String> words = tokenizeDoc(cur_doc);
			HashMap<String, Integer> labelCount = new HashMap<String, Integer>();
			HashMap<String, Integer> labelWordCount = new HashMap<String, Integer>();
			int totalCount = 0;
			for (String label : labels) {
				// only four is considered at the moment.
				if (!label.equals("CCAT") && !label.equals("ECAT")
						&& !label.equals("GCAT") && !label.equals("MCAT"))
					continue;

				String newLabel = "Y=" + label;
				totalCount++;
				if (labelCount.containsKey(newLabel)) {
					int count = labelCount.get(newLabel);
					labelCount.put(newLabel, count + 1);
				} else {
					labelCount.put(newLabel, 1);
				}
				for (String word : words) {
					String mykey = newLabel + ",W=" + word;
					if (labelWordCount.containsKey(mykey)) {
						labelWordCount
								.put(mykey, labelWordCount.get(mykey) + 1);
					} else {
						labelWordCount.put(mykey, 1);
					}
				}
				String allWord = "Y=" + label + ",W=*";
				if (labelWordCount.containsKey(allWord)) {
					labelWordCount.put(allWord, labelWordCount.get(allWord)
							+ words.size());
				} else {
					labelWordCount.put(allWord, words.size());
				}
			}
			word.set("Y=*\t");
			count.set(totalCount);
			output.collect(word, count);
			for (String s : labelCount.keySet()) {
				word.set(s);
				this.count.set(labelCount.get(s));
				output.collect(word, count);
			}
			for (String s : labelWordCount.keySet()) {
				word.set(s);
				this.count.set(labelWordCount.get(s));
				output.collect(word, count);
			}

		}

	}

	public static class Reduce extends MapReduceBase implements
			Reducer<Text, IntWritable, Text, IntWritable> {
		public void reduce(Text key, Iterator<IntWritable> values,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {
			int sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
			}
			output.collect(key, new IntWritable(sum));
		}
	}

	public void run(String input, String output, String num) throws ClassNotFoundException,
			IOException, InterruptedException {
		JobConf conf = new JobConf(NB_train_hadoop.class);
		conf.setJobName("run");
		int nums = Integer.parseInt(num);
		conf.setNumReduceTasks(nums);
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);

		conf.setMapperClass(Map.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(input));
		FileOutputFormat.setOutputPath(conf, new Path(output));

		JobClient.runJob(conf);
	}

}
