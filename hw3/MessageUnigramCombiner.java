import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;


public class MessageUnigramCombiner {
	
	public static void main(String args[]) throws IOException{
		PrintWriter pw = new PrintWriter(System.out);
		BufferedReader bi = new BufferedReader(new InputStreamReader(System.in));
		String line;
		String previous=null;
		int cx = 0;
		bi.readLine();
		bi.readLine();
		String[] extokens  = bi.readLine().split("\t");
		pw.println(extokens[0]+"\tBx="+extokens[1]+"\tCx="+extokens[2]);
		while((line = bi.readLine())!=null){
			String[] tokens = line.split("\t");
			if(tokens.length ==3){
				previous = tokens[0];
				cx = Integer.parseInt(tokens[2]);
			}else{
				String[] words = tokens[1].split(" ");
				if(words[0].equals(previous)){
					pw.println(tokens[1]+"\tCx="+cx);
				}else{
					pw.println(tokens[1]+"\tCy="+cx);
				}
			}
			
		}

		pw.flush();
		pw.close();
	
	
	}
}