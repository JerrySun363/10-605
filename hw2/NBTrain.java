import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.io.PrintWriter;


public class NBTrain {
	public static PrintWriter  pw = new PrintWriter(System.out);

	public static void main(String args[]) throws IOException{
		BufferedReader bi = new BufferedReader(new InputStreamReader(System.in));
		String line;
		//HashMap<String, Integer> labelCount = new HashMap<String, Integer>();
		HashMap<String, Integer> labelWordCount = new HashMap<String, Integer>();

		int totalCount=0;
		while ((line = bi.readLine()) != null){
			String[] parts = line.split("\t");
			String[] labels = parts[0].split(",");
			String cur_doc = parts[1];
			Vector<Integer> words = tokenizeDoc(cur_doc);
			
			for(String label:labels){
				
				String newLabel = "Y="+label;
				totalCount++;
				

				if(labelWordCount.containsKey(newLabel)){
					int count = labelWordCount.get(newLabel);
					labelWordCount.put(newLabel, count+1);
				}else{
					labelWordCount.put(newLabel, 1);
				}

				for(int word:words){
					String key = newLabel+",W="+word;
					if(labelWordCount.containsKey(key)){
						labelWordCount.put(key, labelWordCount.get(key)+1);
					}else{
						labelWordCount.put(key, 1);
					}
				}

				String allWord = "Y="+label+",W="+"*".hashCode();
				if(labelWordCount.containsKey(allWord)){
					labelWordCount.put(allWord, labelWordCount.get(allWord)+words.size());
				}else{
					labelWordCount.put(allWord,words.size());
				}
			}

			if(Runtime.getRuntime().freeMemory()<=20000000){
				printHashMap(labelWordCount);
				labelWordCount.clear();
				
			}
		}
		bi.close();
		pw.println("Y=*\t"+totalCount);
		printHashMap(labelWordCount);
		pw.flush();
		pw.close();
		   
	}
	
	static Vector<Integer> tokenizeDoc(String cur_doc) {
        String[] words = cur_doc.split("\\s+");
        Vector<Integer> tokens = new Vector<Integer>();
        for (int i = 0; i < words.length; i++) {
        	words[i] = words[i].replaceAll("\\W", "");
        	if (words[i].length() > 0) {
        		//System.out.println("Word is :"+ words[i]);
        		tokens.add(words[i].toLowerCase().hashCode());
        	} 
        }
		return tokens;
	}
	
	public static void printHashMap(HashMap<String, Integer> count){
		Set<String> keys = count.keySet();
		for(String s:keys){
			pw.println(s+"\t"+count.get(s));
		}
	}
	
}
