import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.io.PrintWriter;
import java.util.Set;


public class NBTest {
	
	public static HashMap<String, Integer> labelCount = new HashMap<String, Integer>();
	public static HashMap<String,HashMap<Integer, Integer>> labelWordCount = new HashMap<String,HashMap<Integer, Integer>>();
	public static PrintWriter pw = new PrintWriter(System.out);
	public static HashSet<Integer> wordset = new HashSet<Integer>();
	static int match=0;
	static int total=0;

	public static void main(String args[]) throws IOException{
		BufferedReader bi = new BufferedReader(new InputStreamReader(System.in));
		HashSet<Integer> hashset = readTestToSet(args[0]);
		String line;
		/**
		 * Construct the data from below
		 */
		while ((line = bi.readLine()) != null){
			String[] tokens= line.split("\t");
			String label = tokens[0];
			String countString = tokens[1];
			int count = Integer.parseInt(countString);
			parseLabel(label,count,hashset);
		}
		bi.close();
		int vocSize = wordset.size()-labelCount.size()+1;

		wordset = null;
		readTest(args[0],vocSize);
	
		//pw.println("Pre: "+match*1.0/total);
		//pw.println("size is :"+ vocSize);

		pw.flush();
		pw.close();

	}

	private static void parseLabel(String label,int count, HashSet<Integer> hashset) {
		

		if(!label.contains(",")){
			String[] Y = label.split("=");
			labelCount.put(Y[1], count);
		}else{
			String[] parts = label.split(",");
			String[] Y=parts[0].split("=");
			String[] W=parts[1].split("=");
			int code = Integer.parseInt(W[1]);
			wordset.add(code);

			if(!hashset.contains(code) && code!=42){
				return;
			}
			if(labelWordCount.containsKey(Y[1])){
				HashMap<Integer, Integer> wordCount = labelWordCount.get(Y[1]);
				wordCount.put(code, count);
				labelWordCount.put(Y[1], wordCount);
			}else{
				HashMap<Integer, Integer> wordCount = new HashMap<Integer, Integer>();
				wordCount.put(code, count);
				labelWordCount.put(Y[1], wordCount);
			}
				
		}
		
	}

	private static void readTest(String test, int vocSize){
		try {
			BufferedReader bi = new BufferedReader(new InputStreamReader(new FileInputStream(test)));
			String line;
			while((line=bi.readLine())!=null){
				String[] parts = line.split("\t");
				String labels = parts[0];
				String cur_doc = parts[1];
				Vector<Integer> words = NBTrain.tokenizeDoc(cur_doc);
				String result = makePredicition(words, labels, vocSize);
				pw.println(result);
			}
			bi.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static HashSet<Integer> readTestToSet(String test){
		HashSet<Integer> set = new HashSet<Integer>();
		try {
			BufferedReader bi = new BufferedReader(new InputStreamReader(new FileInputStream(test)));
			String line;
			while((line=bi.readLine())!=null){
				String[] parts = line.split("\t");
				String cur_doc = parts[1];
				Vector<Integer> words = NBTrain.tokenizeDoc(cur_doc);
				set.addAll(words);
			}
			bi.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return set;
		
	}

	private static String makePredicition(Vector<Integer> words, String labels, int vocSize) {
		double maxPro = Double.NEGATIVE_INFINITY;
		String maxP ="";
		int totalCount = labelCount.get("*");

		for(String label :labelCount.keySet()){
			if(label.equals("*"))
				continue;

			int article = labelCount.get(label);
			double p = Math.log((article)*1.0/(totalCount));
			
			HashMap<Integer, Integer> dic = labelWordCount.get(label);
			int dicSize = dic.get("*".hashCode());
			dicSize+=vocSize;
			

			for(int word : words){
				int count =1;
				if(dic.containsKey(word)){
					count+=dic.get(word);
				}
				p += Math.log(count*1.0/dicSize);
			}
			
			if(p > maxPro){
				maxPro = p;
				maxP = label;
			}
		}

		if(labels.indexOf(maxP) >= 0){
			match++;
		}
		total++;/**/

		return maxP+"\t"+maxPro;
	}

	public static void printHashMap(HashMap<String, Integer> count){
		Set<String> keys = count.keySet();
		for(String s:keys){
			pw.println(s+"\t"+count.get(s));
		}
	}
}
