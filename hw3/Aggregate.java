import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;


public class Aggregate {
	public static PrintWriter pw = new PrintWriter(System.out);
	public static BufferedReader bi = new BufferedReader(new InputStreamReader(System.in));
	
	//static BufferedReader bi = new BufferedReader(new InputStreamReader(System.in));
	public static void main(String args[]) throws IOException{
		int judge = Integer.parseInt(args[0]);
		String line = "";
		String previous = "";
		int bx = 0;
		int cx = 0;
		long bxtotal =0;
		long cxtotal = 0;

		while((line =bi.readLine())!=null){
			
			String[] tokens = line.split("\t");
			int year = Integer.parseInt(tokens[1]);
			int num = Integer.parseInt(tokens[2]);

			if(tokens[0].equals(previous)){
				if(year < 1990){
					bx += num;
					bxtotal +=num;
				}else{
					cx += num;
					cxtotal +=num;
				}
			}else{
				if(!previous.equals("")){
						pw.println(previous+"\t"+bx+"\t"+cx);
				}
					previous = tokens[0];
					if(year < 1990){
						bx = num;
						cx =0;
						bxtotal += num;
					}else{
						cx = num;
						bx=0;
						cxtotal+=num;
					}
					
				
			}
		}

		
		pw.println(previous+"\t"+bx+"\t"+cx);
		if(judge == 0)
			pw.println("*\t"+bxtotal+"\t"+cxtotal);
		else
			pw.println("* *\t"+bxtotal+"\t"+cxtotal);

		pw.flush();
		pw.close();
		
	}
}
