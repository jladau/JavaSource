package gov.lbnl.IndividualBetaDiversity;

import java.util.ArrayList;
import java.util.Collections;

public class OccurrenceModel{

	/**List of indoor occurrences**/
	private ArrayList<Boolean> lstIndoorOccur;
	 
	/**List of outdoor occurrences**/
	private ArrayList<Boolean> lstOutdoorOccur;
	
	/**List of responses**/
	private ArrayList<Double> lstResponses;
	
	/**Estimates**/
	private Estimates est1;
	
	public OccurrenceModel(
			ArrayList<Boolean> lstIndoorOccur, 
			ArrayList<Boolean> lstOutdoorOccur, 
			ArrayList<Double> lstResponses){
		
		//saving variables
		this.lstIndoorOccur=lstIndoorOccur;
		this.lstOutdoorOccur=lstOutdoorOccur;
		this.lstResponses=lstResponses;
	}
	
	public void loadEstimates(int iNullIterations){
		
		//finding observed estimates
		est1 = new Estimates();
		loadEstimates("observed");
		
		//finding null estimates
		for(int i=0;i<iNullIterations;i++){
			Collections.shuffle(this.lstResponses);
			loadEstimates("null");
		}
	}
	
	public Estimate getEstimate(String sParameter) {
		return est1.get(sParameter);
	}
	
	private void loadEstimates(String sType) {
		
		//rgd1 = totals for I=0,O=0; I=1,O=0; I=0,O=1; I=1,O=1
		//rgd2 = corresponding counts
		//bI = indoor occurrence
		//bO = outdoor occurrence
		//i1 = index for addition
		//d0 = current estimate for d0
		//dI = current estimate for dI
		//dO = current estimate for dO
		//dIO = current estimate for dIO
		//dIPlusIO = current estimate of dI + dIO
		//d0P = placeholder value of d0
		//dIP = placeholder value of dI
		//dOP = placeholder value of dO
		
		int i1;
		double rgd1[];
		double rgd2[];
		boolean bI;
		boolean bO;
		double d0;
		double dI;
		double dO;
		double dIO;
		double dIPlusIO;
		double d0P;
		double dIP;
		double dOP;
		
		rgd1 = new double[4];
		rgd2 = new double[4];
		for(int i=0;i<lstIndoorOccur.size();i++){
			bI = lstIndoorOccur.get(i);
			bO = lstOutdoorOccur.get(i);
			if(bI==false && bO==false){
				i1 = 0;
			}else if(bI==true && bO==false){
				i1 = 1;
			}else if(bI==false && bO==true){
				i1 = 2;
			}else{
				i1 = 3;
			}
			rgd1[i1]+=lstResponses.get(i);
			rgd2[i1]+=1;
		}
		
		if(rgd2[0]>0){
			d0 = rgd1[0]/rgd2[0];
			d0P = d0;
		}else{
			d0 = Double.NaN;
			d0P = 0;
		}
		if(rgd2[1]>0){
			dI = rgd1[1]/rgd2[1]-d0P;
			dIP = dI;
		}else {
			dI = Double.NaN;
			dIP = 0;
		}
		if(rgd2[2]>0){
			dO = rgd1[2]/rgd2[2]-d0P;
			dOP = dO;
		}else{
			dO= Double.NaN;
			dOP = 0;
		}
		if(rgd2[3]>0){
			dIO = rgd1[3]/rgd2[3]-d0P-dIP-dOP;
		}else {
			dIO = Double.NaN;
		}
		if(!Double.isNaN(dI) && !Double.isNaN(dIO)){
			dIPlusIO = dI + dIO;
		}else {
			dIPlusIO = Double.NaN;
		}
		
		
		est1.set("d0", sType, d0);
		est1.set("dI", sType, dI);
		est1.set("dO", sType, dO);
		est1.set("dIO", sType, dIO);
		est1.set("dIPlusIO", sType, dIPlusIO);
	}
	
	public class Estimates{
		
		/**Parameter estimate for intercept**/
		private Estimate est0;
		
		/**Parameter estimate indoor occurrence**/
		private Estimate estI;
		
		/**Parameter estimate outdoor occurrence**/
		private Estimate estO;
		
		/**Parameter estimate for indoor-outdoor interaction**/
		private Estimate estIO;
		
		/**Parameter estimate for indoor effect**/
		private Estimate estIPlusIO;
		
		public Estimates(){
			est0 = new Estimate();
			estI = new Estimate();
			estO = new Estimate();
			estIO = new Estimate();
			estIPlusIO = new Estimate();
		}
		
		public Estimate get(String sParameter){
			if(sParameter.equals("d0")){
				return est0;
			}else if(sParameter.equals("dI")){
				return estI;
			}else if(sParameter.equals("dO")){
				return estO;
			}else if(sParameter.equals("dIO")){
				return estIO;
			}else if(sParameter.equals("dIPlusIO")){
				return estIPlusIO;
			}else {
				return null;
			}
		}
		
		public void set(String sParameter, String sType, double dEstimate) {
			if(sParameter.equals("d0")){
				est0.set(sType,dEstimate);
			}else if(sParameter.equals("dI")){
				estI.set(sType,dEstimate);
			}else if(sParameter.equals("dO")){
				estO.set(sType,dEstimate);
			}else if(sParameter.equals("dIO")){
				estIO.set(sType,dEstimate);
			}else if(sParameter.equals("dIPlusIO")){
				estIPlusIO.set(sType,dEstimate);
			}
		}
	}
	
	public class Estimate{
		
		/**Value**/
		private double dEstimate;
		
		/**Null total**/
		private double dNullTotal=0;
		
		/**Null total squared**/
		private double dNullTotal2=0;
		
		/**Null count**/
		private double dN=0;
		
		/**Null count greater than or equal to observed value**/
		private double dGTE=0;
		
		/**Null count less than or equal to observed value**/
		private double dLTE=0;
		
		public Estimate(){
			
		}
		
		public String toString(){
			return get("value") + "," + get("ses") + "," + get("probability_gte") + "," + get("probability_lte");
		}
		
		public void set(String sType, double dEstimate){
			if(sType.equals("observed")){
				this.dEstimate = dEstimate;
			}else if(sType.equals("null")){
				dNullTotal+=dEstimate;
				dNullTotal2+=(dEstimate*dEstimate);
				dN++;
				if(dEstimate>=this.dEstimate){
					dGTE++;
				}
				if(dEstimate<=this.dEstimate){
					dLTE++;
				}
			}
		}
		
		public double get(String sStatistic){
			
			//d1 = standard deviation
			
			double d1;
			
			if(sStatistic.equals("value")){
				return dEstimate;
			}else if(sStatistic.equals("ses")){
				
				
				
				d1 = Math.sqrt((dN*dNullTotal2 - dNullTotal*dNullTotal)/(dN*(dN-1.)));
				
				
				//*************************
				System.out.println(dNullTotal/dN + "," + (dNullTotal/dN)/d1);
				//TODO SES from 0 rather than null mean?
				//*************************
				
				return (dEstimate - dNullTotal/dN)/d1;
			}else if(sStatistic.equals("probability_gte")){
				return dGTE/dN;
			}else if(sStatistic.equals("probability_lte")){
				return dLTE/dN;
			}else {
				return Double.NaN;
			}
		}	
	}
}