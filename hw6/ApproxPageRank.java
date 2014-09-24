import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class ApproxPageRank {
	BufferedReader br = null;
	String seed;
	float alpha;
	float epsilon;
	HashMap<String, Float> r;
	HashMap<String, Float> p;
	HashMap<String, List<String>> edges;
	boolean isContinue = true;
	int total;
	long start = 0;

	public ApproxPageRank(String path, String seed, float alpha, float epsilon) {
		this.seed = seed;
		this.alpha = alpha;
		this.epsilon = epsilon;
		this.p = new HashMap<String, Float>();
		this.r = new HashMap<String, Float>();
		this.edges = new HashMap<String, List<String>>();
		this.r.put(seed, 1.0f);
		this.p.put(seed, 0.0f);
		this.edges.put(seed, new LinkedList<String>());
		this.total = 0;

	}

	public void readData() {
		String line;
		try {
			this.isContinue = false;

			while ((line = br.readLine()) != null) {
				// String tokens[] = line.split("\t", 2);
				String first = line.substring(0, line.indexOf('\t'));
				if (!r.containsKey(first)) {
					continue;
				}

				String[] mytokens = line.split("\t");
				int neighbour = mytokens.length - 1;

				if ((r.get(first) / neighbour) < this.epsilon) {
					continue;
				}

				if (edges.containsKey(first) && edges.get(first).isEmpty()) {
					List<String> stringList = edges.get(first);

					for (int i = 1; i < mytokens.length; i++) {
						stringList.add(mytokens[i]);
					}

					/*
					 * for (String s : mytokens) { stringList.add(s); }
					 */
				}

				this.isContinue = true;
				// update score of p
				float score = this.alpha * r.get(first);
				if (p.containsKey(first)) {
					p.put(first, p.get(first) + score);
				} else {
					p.put(first, score);
					edges.put(first, new LinkedList<String>());
				}

				// update R
				float newvalue = (1 - this.alpha) * r.get(first)
						/ (2 * neighbour);

				for (int i = 1; i < mytokens.length; i++) {
					if (r.containsKey(mytokens[i])) {
						r.put(mytokens[i], r.get(mytokens[i]) + newvalue);
					} else {
						r.put(mytokens[i], newvalue);
					}
				}

				/*
				 * for (String s : mytokens) { if (r.containsKey(s)) { r.put(s,
				 * r.get(s) + newvalue); } else { r.put(s, newvalue); } }
				 */

				r.put(first, newvalue * neighbour);
			}

			br.close();
			br = null;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initReader(String path) {
		try {
			br = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void calculateSubgraph(HashMap<String, Float> p, String seed) {
		List<Entry<String, Float>> pvalues = new LinkedList<Entry<String, Float>>(
				p.entrySet());
		Collections.sort(pvalues, new Comparator<Entry<String, Float>>() {
			@Override
			public int compare(Entry<String, Float> o1, Entry<String, Float> o2) {
				// rank them in descending order
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		// now pvalues are ranked from top to low.
		// init value
		HashSet<String> S = new HashSet<String>();

		S.add(seed);

		double best = 1.0;
		int volume = this.edges.get(seed).size();
		int boundary = this.edges.get(seed).size();

		String stop = "";
		// ArrayList<String> totalEdges = new ArrayList<String>();

		for (Entry<String, Float> entry : pvalues) {
			String toUse = entry.getKey();

			if (toUse.equals(seed)) {
				continue;
			}

			List<String> edges = this.edges.get(toUse);

			int newvolume = volume + edges.size();
			int newboundary = boundary + edges.size();
			for (String node : edges) {
				if (S.contains(node)) {
					newboundary -= 2;
				}
			}
			double newValue = newboundary * 1.0 / newvolume;
			volume = newvolume;
			boundary = newboundary;
			if (newValue <= best) {
				best = newValue;
				stop = toUse;
			}
			S.add(toUse);
		}

		this.printPageRank(stop, pvalues);

	}

	public void printPageRank(String stop, List<Entry<String, Float>> pvalues) {
		PrintWriter pw = new PrintWriter(System.out);
		for (Entry<String, Float> s : pvalues) {
			String node = s.getKey();
			pw.println(node + "\t" + p.get(node));
			if (node.equals(stop)) {
				break;
			}
		}
		//pw.println((System.currentTimeMillis() - start) * 1.0 / 1000);
		pw.flush();
		pw.close();

	}

	/*
	 * public void printGraph(String stop, List<Entry<String, Float>> pvalues) {
	 * PrintWriter pw = new PrintWriter(System.out); ArrayList<String>
	 * totalEdges = new ArrayList<String>();
	 * pw.println("nodedef>node VARCHAR, weight DOUBLE"); for (Entry<String,
	 * Float> s : pvalues) { String node = s.getKey(); List<String> edgeList =
	 * this.edges.get(node); for (String ss : edgeList) { totalEdges.add(node +
	 * "," + ss); } double score = Math.log(this.p.get(node) / this.epsilon);
	 * score = 1 > score ? 1 : score; pw.println(node + "," + score);//
	 * +"\t"+Ss.contains(node)); if (node.equals(stop)) { break; } }
	 * pw.println("edgedef>node1 VARCHAR,node2 VARCHAR"); for (String s :
	 * totalEdges) { pw.println(s); if (s.startsWith(stop)) { break; } }
	 * pw.flush(); pw.close();
	 * 
	 * }
	 */

	public static void main(String args[]) {
		//Long start = System.currentTimeMillis();

		String path = args[0];
		String seed = args[1];
		float alpha = Float.parseFloat(args[2]);
		float epsilon = Float.parseFloat(args[3]);
		ApproxPageRank apr = new ApproxPageRank(path, seed, alpha, epsilon);
		//apr.start = start;
		while (apr.isContinue) {
			apr.initReader(path);
			apr.readData();
		}
		apr.calculateSubgraph(apr.p, seed);

	}
}
