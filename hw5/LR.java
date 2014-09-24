import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

/*
 * Array Version implementation; Use as much array as possible.
 * */
public class LR {
	public int vocSize;
	public double learnRate;
	public double cof;
	public int interation;
	public int size;
	public String file;
	public BufferedReader bi;
	static double overflow = 20;
	public int k = 0;
	public HashMap<String, int[]> A;
	public HashMap<String, double[]> B;
	public int round = 0;
	public HashMap<String, Double> LCL;
	public HashSet<String> stop;
	private HashMap<String, Double> newLCL;

	public LR(int vocSize, double learnRate, double cof, int interation,
			int size, String file) {
		super();
		this.vocSize = vocSize;
		this.learnRate = learnRate;
		this.cof = cof;
		this.interation = interation;
		this.size = size;
		this.file = file;
		this.bi = new BufferedReader(new InputStreamReader(System.in));
		
	}

	public void init() {
		/*
		 * Initialize the hash tables of labels
		 */
		this.A = new HashMap<String, int[]>();
		this.B = new HashMap<String, double[]>();
		this.LCL = new HashMap<String, Double>();
		this.newLCL = new HashMap<String, Double>();
		this.stop = new HashSet<String>();
		
		A.put("ca", new int[this.vocSize]);
		A.put("de", new int[this.vocSize]);
		A.put("es", new int[this.vocSize]);
		A.put("el", new int[this.vocSize]);
		A.put("fr", new int[this.vocSize]);
		A.put("hr", new int[this.vocSize]);
		A.put("hu", new int[this.vocSize]);
		A.put("nl", new int[this.vocSize]);
		A.put("pt", new int[this.vocSize]);
		A.put("pl", new int[this.vocSize]);
		A.put("ru", new int[this.vocSize]);
		A.put("ga", new int[this.vocSize]);
		A.put("sl", new int[this.vocSize]);
		A.put("tr", new int[this.vocSize]);

		B.put("ca", new double[this.vocSize]);
		B.put("de", new double[this.vocSize]);
		B.put("es", new double[this.vocSize]);
		B.put("el", new double[this.vocSize]);
		B.put("fr", new double[this.vocSize]);
		B.put("hr", new double[this.vocSize]);
		B.put("hu", new double[this.vocSize]);
		B.put("nl", new double[this.vocSize]);
		B.put("pt", new double[this.vocSize]);
		B.put("pl", new double[this.vocSize]);
		B.put("ru", new double[this.vocSize]);
		B.put("ga", new double[this.vocSize]);
		B.put("sl", new double[this.vocSize]);
		B.put("tr", new double[this.vocSize]);
		
		LCL.put("ca", 100000.0);
		LCL.put("de", 100000.0);
		LCL.put("es", 100000.0);
		LCL.put("el", 100000.0);
		LCL.put("fr", 100000.0);
		LCL.put("hr", 100000.0);
		LCL.put("hu", 100000.0);
		LCL.put("nl", 100000.0);
		LCL.put("pt", 100000.0);
		LCL.put("pl", 100000.0);
		LCL.put("ru", 100000.0);
		LCL.put("ga", 100000.0);
		LCL.put("sl", 100000.0);
		LCL.put("tr", 100000.0);
		
		
		newLCL.put("ca", 0.0);
		newLCL.put("de", 0.0);
		newLCL.put("es", 0.0);
		newLCL.put("el", 0.0);
		newLCL.put("fr", 0.0);
		newLCL.put("hr", 0.0);
		newLCL.put("hu", 0.0);
		newLCL.put("nl", 0.0);
		newLCL.put("pt", 0.0);
		newLCL.put("pl", 0.0);
		newLCL.put("ru", 0.0);
		newLCL.put("ga", 0.0);
		newLCL.put("sl", 0.0);
		newLCL.put("tr", 0.0);

	}

	public void readData() {
		String line;
		int y = 0;
		try {
			while ((line = bi.readLine()) != null) {
				this.round = this.k / this.size + 1;
				this.k++;

				String[] tokens = line.split("\t");
				String[] labels = tokens[0].split(",");
				HashSet<String> ones = new HashSet<String>();
				for (String label : labels) {
					ones.add(label);
				}
				String[] features = tokens[1].split(" ");

				int[] indice = new int[features.length];
				int i = 0;
				for (String feature : features) {
					indice[i] = this.convert(feature);
					i++;
				}

				for (String key : this.B.keySet()) {
					if(this.stop.contains(key)){
						//should not update any more;
						continue;
					}
					
					if (ones.contains(key)) {
						y = 1;
					} else {
						y = 0;
					}

					int[] Akey = A.get(key);
					double[] Bkey = B.get(key);
			    	// now calculate p
					//System.out.println("Round is "+ this.round + "Learning rate is"+ this.learnRate
					//		/ (this.round * this.round) );
					
					double p = 0;
					for (int index : indice) {
						p += Bkey[index];
						Bkey[index] = Bkey[index]
								* Math.pow(
										(1 - this.learnRate
												/ (this.round * this.round)
												* this.cof), 
												this.k - Akey[index]);

						
					}
					
					
					p = this.sigmoid(p);
					//System.out.println(p);

					double lcl = this.newLCL.get(key);
					lcl += y==1?Math.log(p):Math.log(1-p);
					//System.out.println(lcl);
					this.newLCL.put(key, lcl);
					
					// update Bkey
					for (int index : indice) {
						Bkey[index] += this.learnRate
								/ (this.round * this.round) * (y - p);
						Akey[index] = this.k;
					}

				}
				
				if(this.k % this.size == 0){
					this.updateLCL();
					if(this.stop.size() == 14){
						return;
					}
				}

			}
			/*
			 * finally we should update those have not been touched till last
			 * update to the end
			 */
			for (String key : B.keySet()) {
				if(this.stop.contains(key)){
					continue;
				}
				int[] Akey = A.get(key);
				double[] Bkey = B.get(key);
				for (int i = 0; i < this.vocSize; i++) {
					Bkey[i] = Bkey[i]
							* Math.pow((1 - this.learnRate
									/ (this.round * this.round) * this.cof),
									this.k - Akey[i]);
				}
			}
			bi.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void makePrediction() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(this.file)));
			PrintWriter pw = new PrintWriter(System.out);
			String line;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				String[] features = tokens[1].split(" ");
				int[] indice = new int[features.length];
				int i = 0;
				for (String feature : features) {
					indice[i] = this.convert(feature);
					i++;
				}
				
				String result = "";
			    for(String label : B.keySet()){
			    	//System.out.println("I am predicting! " + label);
			    	double[] parameters = B.get(label);
			    	double score = 0;
			    	for(int index : indice){
			    		score += parameters[index];
			    	}
			    	double p = this.sigmoid(score); 
			    	result += ","+label +"\t"+p;

			    }
			    
				result= result.substring(1);
				pw.println(result);
			}
			br.close();
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected double sigmoid(double score) {
		if (score > overflow)
			score = overflow;
		else if (score < -overflow)
			score = -overflow;
		double exp = Math.exp(score);
		return exp / (1 + exp);
	}

	public int convert(String text) {
		int id = text.hashCode() % this.vocSize;
		if (id < 0)
			id += this.vocSize;
		return id;
	}
	
	public void updateLCL(){
		 	for(String key:this.B.keySet()){
		 		if(this.stop.contains(key)){
		 			continue;
		 		}
		 		double lastNorm = this.LCL.get(key);
		 		double thisLCL = this.newLCL.get(key);
		 		double norm  = 0;
		 		for(double feature : B.get(key)){
		 			norm += feature * feature;
		 		}
		 		norm *= this.cof/2;
		 		thisLCL -= norm;
		 		if( Math.abs(Math.abs(thisLCL- lastNorm)/lastNorm) <= 0.001){
		 			this.stop.add(key);
		 		}
		 		this.LCL.put(key, thisLCL);
		 		//System.out.println(key +"\t" + Math.abs(thisLCL - lastNorm)/Math.abs(lastNorm));
		 		this.newLCL.put(key, 0.0);
		 	}
	}

	public static void main(String args[]) {
		int vocSize = Integer.parseInt(args[0]);
		double learnRate = Double.parseDouble(args[1]);
		double cof = Double.parseDouble(args[2]);
		int interation = Integer.parseInt(args[3]);
		int size = Integer.parseInt(args[4]);
		String file = args[5];
		LR lr = new LR(vocSize, learnRate, cof, interation, size, file);
		lr.init();
		lr.readData();
		lr.makePrediction();
		
		
		
		
	}

}
