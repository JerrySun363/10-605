import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;


public class PhraseGenerator {
	
	public static class Node implements Comparable<Node>{
		public String phrase;
		public double totalScore;
		public double phraseScore;
		public double infoScore;
		
		public Node(String phrase,double phraseScore, double infoScore){
			this.phrase = phrase;
			this.phraseScore = phraseScore;
			this.infoScore = infoScore;
			this.totalScore = this.phraseScore + this.infoScore;
		}

		@Override
		public int compareTo(Node o) {
			if(this.totalScore<o.totalScore)
				return -1;
			else if(this.totalScore>o.totalScore)
				return 1;
			else
				return 0;
		}
	} 
		
	public static void main(String args[]) throws IOException{
		
		PrintWriter pw = new PrintWriter(System.out);
		BufferedReader bi = new BufferedReader(new InputStreamReader(System.in));
		
		String line="";
		int bxy=0,cxy = 0,cx=0,cy = 0;
		int counter =0;
		String first = bi.readLine();
		String[] mytokens = first.split("\t");
		String[] words2 = mytokens[2].split("=");
		long ctotal = Long.parseLong(words2[1]);
		
		first = bi.readLine();
		mytokens = first.split("\t");
		long bxytotal = Long.parseLong(mytokens[1]);
		long cxytotal = Long.parseLong(mytokens[2]);
		PriorityQueue<Node> queue = new PriorityQueue<Node>(20);
		
		
		while((line = bi.readLine())!=null){
			String[] tokens = line.split("\t");
			
			if(counter == 0){
				bxy = Integer.parseInt(tokens[1]);
				cxy = Integer.parseInt(tokens[2]);
				counter++;
			}else if(counter ==1){
				 words2 = tokens[1].split("=");
			     cx = Integer.parseInt(words2[1]);
				 counter++;
			}else{
					words2 = tokens[1].split("=");
					cy = Integer.parseInt(words2[1]);
					counter=0;
				double p = (1+cxy)*1.0/cxytotal;
				double q = (1+cx)*1.0/ctotal *(1+cy)/ctotal;
				double phraseScore = p *Math.log(p/q);
				q = (1+bxy)*1.0/bxytotal;
				double infoScore = p* Math.log(p/q);
				
				if(queue.size()<20){
					queue.add(new Node(tokens[0],phraseScore,infoScore));
				}else if(queue.peek().totalScore < (infoScore+phraseScore)){
					queue.add(new Node(tokens[0],phraseScore,infoScore));
					queue.poll();
				}
				
				
			}
		}
		bi.close();
		
		Node[] myarray= queue.toArray(new Node[20]);
		Arrays.sort(myarray);
		for(int i=19;i>=0;i--){
			pw.println(myarray[i].phrase+"\t"+myarray[i].totalScore+"\t"+myarray[i].phraseScore+"\t"+myarray[i].infoScore);
		}
		pw.flush();
		pw.close();
	
		
	}
	
}
