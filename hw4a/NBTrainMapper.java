import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.io.PrintWriter;


public class NBTrainMapper {
	public static PrintWriter  pw = new PrintWriter(System.out);

	public static void main(String args[]) throws IOException{
		BufferedReader bi = new BufferedReader(new InputStreamReader(System.in));
		String line;
		HashMap<String, Integer> labelCount = new HashMap<String, Integer>();
		HashMap<String, Integer> labelWordCount = new HashMap<String, Integer>();

		int totalCount=0;
		while ((line = bi.readLine()) != null){
			String[] parts = line.split("\t");
			String[] labels = parts[0].split(",");
			String cur_doc = parts[1];
			Vector<String> words = tokenizeDoc(cur_doc);
			
			for(String label:labels){
				//only four is considered at the moment.
				if(!label.equals("CCAT") && !label.equals("ECAT") && !label.equals("GCAT") && !label.equals("MCAT"))
					continue;
				
				String newLabel = "Y="+label;
				totalCount++;
				if(labelCount.containsKey(newLabel)){
					int count = labelCount.get(newLabel);
					labelCount.put(newLabel, count+1);
				}else{
					labelCount.put(newLabel, 1);
				}
				for(String word:words){
					String key = newLabel+",W="+word;
					if(labelWordCount.containsKey(key)){
						labelWordCount.put(key, labelWordCount.get(key)+1);
					}else{
						labelWordCount.put(key, 1);
					}
				}
				String allWord = "Y="+label+",W=*";
				if(labelWordCount.containsKey(allWord)){
					labelWordCount.put(allWord, labelWordCount.get(allWord)+words.size());
				}else{
					labelWordCount.put(allWord,words.size());
				}
			}
		}
		bi.close();
		pw.println("Y=*\t"+totalCount);

		printHashMap(labelCount);
		printHashMap(labelWordCount);
		pw.flush();
		pw.close();
		   
	}
	
	static Vector<String> tokenizeDoc(String cur_doc) {
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
	
	static void printHashMap(HashMap<String, Integer> count){
		Set<String> keys = count.keySet();
		for(String s:keys){
			pw.println(s+"\t"+count.get(s));
		}
	}
	
}
