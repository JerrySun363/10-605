import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;


public class MessageGenerator {
	
	public static void main(String args[]) throws IOException{
		String line;
		PrintWriter pw = new PrintWriter(System.out);
		BufferedReader bi = new BufferedReader(new InputStreamReader(System.in));
		
		while((line= bi.readLine())!=null){
			String tokens[] = line.split("\t");
			String[] words = tokens[0].split(" ");
			pw.println(words[0]+"\t"+tokens[0]);
			pw.println(words[1]+"\t"+tokens[0]);
		}
		pw.flush();
		pw.close();
		
	}
}
