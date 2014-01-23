import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;


public class NBTest {
	
	public static HashMap<String, Integer> labelCount = new HashMap<String, Integer>();
	public static HashMap<String,HashMap<String, Integer>> labelWordCount = new HashMap<String,HashMap<String, Integer>>();
	
	public static void main(String args[]) throws IOException{
		BufferedReader bi = new BufferedReader(new InputStreamReader(System.in));
		String line;
		/**
		 * Construct the data from below
		 */
		while ((line = bi.readLine()) != null){
			String[] tokens= line.split("\t");
			String label = tokens[0];
			String countString = tokens[1];
			int count = Integer.parseInt(countString);
			parseLabel(label,count);
			readTest(args[0]);
		}
		bi.close();
	}

	private static void parseLabel(String label,int count) {
		
		if(!label.contains(",")){
			String[] Y = label.split("=");
			labelCount.put(Y[1], count);
		}else{
			String[] parts = label.split(",");
			String[] Y=parts[0].split("=");
			String[] W=parts[1].split("=");
			if(labelWordCount.containsKey(Y[1])){
				HashMap<String, Integer> wordCount = labelWordCount.get(Y[1]);
				wordCount.put(W[1], count);
			}else{
				HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
				wordCount.put(W[1], count);
				labelWordCount.put(Y[1], wordCount);
			}
				
		}
		
	}

	private static void readTest(String test){
		try {
			BufferedReader bi = new BufferedReader(new InputStreamReader(new FileInputStream(test)));
			String line;
			while((line=bi.readLine())!=null){
				String[] parts = line.split("\t");
				//String[] labels = parts[0].split(",");
				String cur_doc = parts[1].trim();
				Vector<String> words = NBTrain.tokenizeDoc(cur_doc);
				String result = makePredicition(words);
				System.out.println(result);
				
			}
			bi.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String makePredicition(Vector<String> words) {
		double maxPro = Double.MIN_VALUE;
		String maxP ="";
		int totalCount = labelCount.get("*");
		
		for(String label :labelCount.keySet()){
			if(label.equals("."))
				continue;
			int article = labelCount.get(label);
			double p = Math.log((article+1)*1.0/(totalCount+4));
			HashMap<String, Integer> dic = labelWordCount.get(label);
			int dicSize = dic.get("*");
			dicSize+=dic.size()-1;
			
			for(String word : words){
				int count =1;
				if(word.equals("*"))
					continue;
				
				if(dic.containsKey(word))
					count+=dic.get(word);
				
				p += Math.log(count*1.0/dicSize);
				
			}
			
			if(p>maxPro){
				maxPro = p;
				maxP = label;
			}
		}
		
		return maxP+"\t"+maxPro;
	}
}
