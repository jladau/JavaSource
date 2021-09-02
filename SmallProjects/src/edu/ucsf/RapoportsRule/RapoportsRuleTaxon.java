package edu.ucsf.RapoportsRule;

import com.google.common.collect.Range;

public class RapoportsRuleTaxon{
	
	/**True range of taxon**/
	public Range<Double> rngRangeTrue;
	
	/**ID of taxon**/
	public int iID;
	
	/**Observed range**/
	public Range<Double> rngRangeObserved=null;
	
	/**Observed maximum possible range**/
	public Range<Double> rngRangeMaximumPossibleObserved=null;
	
	
	/**True prevalence**/
	public int iTruePrevalence;
	
	/**Observed prevalence**/
	public int iObservedPrevalence;
	
	public RapoportsRuleTaxon(Range<Double> rngRange, int iID){
		this.iID = iID;
		this.rngRangeTrue = rngRange;
		this.iTruePrevalence = 0;
		this.iObservedPrevalence = 0;
	}
	
	public void addToObservedRange(Double dLat){
		if(rngRangeObserved==null){
			rngRangeObserved = Range.closed(dLat, dLat);
		}else{
			rngRangeObserved = rngRangeObserved.span(Range.closed(dLat, dLat));
		}
	}
	
	public void addToMaximumPossiblObservedRange(Double dLat){
		if(rngRangeMaximumPossibleObserved==null){
			rngRangeMaximumPossibleObserved = Range.closed(dLat, dLat);
		}else{
			rngRangeMaximumPossibleObserved = rngRangeMaximumPossibleObserved.span(Range.closed(dLat, dLat));
		}
	}
	
	public double observedMaxPossibleRangeBreadth(){
		return rngRangeMaximumPossibleObserved.upperEndpoint()-rngRangeMaximumPossibleObserved.lowerEndpoint();
	}
	
	public double observedRangeBreadth(){
		return rngRangeObserved.upperEndpoint()-rngRangeObserved.lowerEndpoint();
	}
	
	public double trueRangeBreadth(){
		return rngRangeTrue.upperEndpoint()-rngRangeTrue.lowerEndpoint();
	}
	
}
