import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;


public class MergeCounts {
	public static PrintWriter pw = new PrintWriter(System.out);
	
	public static void main(String args[]){
		BufferedReader bi = new BufferedReader(new InputStreamReader(System.in));
		
		String line;
		try {
			String myLabel = "";
			int myCount = 0;
			int instanceCount = 0;
			
			while ((line = bi.readLine()) != null){
				String[] tokens= line.split("\t");
				String label = tokens[0];
				String countString = tokens[1];
				int count = Integer.parseInt(countString);
				if(label.equals(myLabel)){
					//System.out.println("I got an equal here!");
					myCount+=count;
				}else{
					if(myLabel.equals("")){
						myLabel = label;
						myCount = count;
						//System.out.println("Can I go here?");
						continue;
					}

					pw.println(myLabel+"\t"+myCount);
					instanceCount++;
					myLabel = label;
					myCount = count;
					
				}
			}
			bi.close();
			//pw.println("Y=*"+instanceCount);
			pw.println(myLabel+"\t"+myCount);
			pw.flush();
			pw.close();
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
	
