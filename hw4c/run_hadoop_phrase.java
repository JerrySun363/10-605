import java.io.IOException;


public class run_hadoop_phrase {
	public static void main(String args[]){
		String unigram = args[0];
		String bigram = args[1];
		String aggregated = args[2];
		String sizeCount = args[3];
		String unigramMessage = args[4];
		String output = args[5];
		try {
			Aggregate.run(unigram,aggregated+"unigram",0+"");
			long ctotal = Aggregate.cxCount;
			long cVol = Aggregate.cxVol;
			Aggregate.run(bigram,aggregated+"bigram",1+"");
			long bxytotal = Aggregate.bxCount;
			long cxytotal = Aggregate.cxCount;
			long bxyVol = Aggregate.bxVol;
			long cxyVol = Aggregate.cxVol;
			Message_Unigram.run(aggregated+"unigram", aggregated+"bigram", unigramMessage);
			Compute.run(aggregated+"bigram", unigramMessage, output, cxytotal, bxytotal, ctotal,cxyVol, bxyVol, cVol);
			
		} catch (ClassNotFoundException | IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
