package edu.ucsf.RapoportsRule;

public class RapoportsRuleSample{
	
	/**Latitude**/
	public double dLat;
	
	/**ID of sample**/
	public int iID;
	
	/**Richness**/
	public double dRichness;
	
	public RapoportsRuleSample(double dLat, int iID){
		this.iID = iID;
		this.dLat = dLat;
		dRichness = 0;
	}
	
	public void incrementRichness(){
		dRichness++;
	}
}