package edu.ucsf.geospatial;

import java.util.ArrayList;
import java.util.HashSet;
import static java.lang.Math.*;
import edu.ucsf.geospatial.RawSurveyResults.RawSurveyResult;
import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.unittested.base.ExtendedMath;

/**
 * Sampling region for beta-diversity range shapes analysis
 * @author jladau
 */

public class SphericalCapEarth_SamplingRegion extends SphericalCapEarth{

	/**Number of paired samples**/
	private int iPairedSamples = 0;
	
	/**Number of unpaired samples**/
	private int iUnpairedSamples = 0;
	
	/**Sorenson beta-diversity**/
	private double dBs = Double.NaN;
	
	/**Jaccard beta-diversity**/
	private double dBj = Double.NaN;
	
	/**Whittaker beta-diversity**/
	private double dBw = Double.NaN;
	
	/**Regional total area**/
	private double dRegionalAreaTotal = 0.;
	
	/**Global area**/
	private double dGlobalAreaTotal = 0.;
	
	/**Global scaled area**/
	private double dGlobalScaledAreaTotal = 0.;
	
	/**Regional total perimeter**/
	private double dRegionalPerimeterTotal = 0.;
	
	/**Global perimeter**/
	private double dGlobalPerimeterTotal = 0.;
	
	/**Global scaled perimeter**/
	private double dGlobalScaledPerimeterTotal = 0.;
	
	/**Mean alpha diversity**/
	private double dAlpha = 0.;
	
	/**Total number of species**/
	protected int iNumberOfSpecies = 0;
	
	/**Cap for calculating Global area and perimeter**/
	private SphericalCapEarth capGlobal = null;
	
	public SphericalCapEarth_SamplingRegion(double dRadius, double dLatCenter, double dLonCenter, double dGlobalRadius, int iRandomSeed){
		super(dRadius, dLatCenter, dLonCenter, iRandomSeed);
		if(!Double.isNaN(dGlobalRadius) && dGlobalRadius<=EarthGeometry.EARTH_CIRCUMFERENCE_SPHERE/4.){
			capGlobal = new SphericalCapEarth(dGlobalRadius, dLatCenter, dLonCenter, iRandomSeed+1);
		}
	}
	
	
	public void addPerimeterArea(SphericalMultiPolygon ply1, double dGlobalPerimeter, double dGlobalArea){
		
		//dL = current Global or global perimeter
		//dF = current Global or global area
		
		//if Global radius > earth radius then reuse perimeter/area within caps across all sampling regions; if Global radius < earth radius then recalculate perimeter/area for each sampling region
		
		double dF;
		double dL;
		
		if(ply1.intersects(this)){
			
			iNumberOfSpecies++;
			
			dL = ply1.perimeter(this);
			dF = ply1.area(this);			
			dRegionalPerimeterTotal+=dL;
			dRegionalAreaTotal+=dF;
			
			dF=dGlobalArea;
			dL=dGlobalPerimeter;
			dGlobalScaledAreaTotal+=area()*dF/(area()+dF+perimeter()*dL/(2.*PI));
			dGlobalScaledPerimeterTotal+=area()*dL/(area()+dF+perimeter()*dL/(2.*PI));
			dGlobalAreaTotal+=dF;
			dGlobalPerimeterTotal+=dL;
		}
	}
	
	public void loadBetaDiversity(RawSurveyResults rss1){
		
		//setUnion = set of all species observed in all first samples
		//lstAlpha = list of alpha-diversity values for all first samples
		//lstBs = list of sorenson values
		//lstBj = list of jaccard values
		//dA = intersection size
		//rsr1 = current first result
		//rsr2 = current second result
		
		RawSurveyResult rsr1;
		RawSurveyResult rsr2;
		HashSet<String> setUnion;
		ArrayList<Double> lstAlpha;
		ArrayList<Double> lstBs;
		ArrayList<Double> lstBj;
		double dA;
		
		//initializing
		setUnion = new HashSet<String>(1000);
		lstAlpha = new ArrayList<Double>(1000);
		lstBs = new ArrayList<Double>(1000);
		lstBj = new ArrayList<Double>(1000);
		
		//looping through sampling plots
		for(String sSample1:rss1.keySet()){
			
			//checking if first sample
			if(sSample1.endsWith("-0")){
				
				//checking if first sample is within sampling region
				rsr1 = rss1.get(sSample1);
				if(this.contains(rsr1.latitude(),rsr1.longitude())){
					
					//updating list of all species in sampling region (gamma-diversity)
					setUnion.addAll(rsr1.speciesSet());
					
					//updating list of alpha diversity
					lstAlpha.add((double) rsr1.size());
					
					//checking if second sample is within sampling region
					rsr2 = rss1.get(sSample1.replace("-0", "-1"));
					if(this.contains(rsr2.latitude(), rsr2.longitude())){
						
						//loading intersection size
						dA = 0;
						if(rsr2.size()<rsr1.size()){
							for(String s:rsr2.speciesSet()){
								if(rsr1.speciesSet().contains(s)){
									dA++;
								}
							}
						}else{
							for(String s:rsr1.speciesSet()){
								if(rsr2.speciesSet().contains(s)){
									dA++;
								}
							}
						}
						
						//saving jaccard and sorenson
						if(rsr1.speciesSet().size()>0 || rsr2.speciesSet().size()>0){
							lstBj.add(1.-dA/((double) (rsr1.speciesSet().size() + rsr2.speciesSet().size() - dA)));
							lstBs.add(1.-2*dA/((double) (rsr1.speciesSet().size() + rsr2.speciesSet().size())));
						}//else{
						//	lstBj.add(1.);
						//	lstBs.add(1.);
						//}
					}
				}
			}
		}
		
		//saving results
		if(setUnion.size()==0){
			dBw = -9999;
			dBs = -9999;
			dBj = -9999;
			dAlpha = 0.;
			iUnpairedSamples=0;
			iPairedSamples=0;
		}else{
			dAlpha = ExtendedMath.mean(lstAlpha);
			dBw = setUnion.size()/dAlpha;
			dBs = ExtendedMath.mean(lstBs);
			dBj = ExtendedMath.mean(lstBj);
			iUnpairedSamples = lstAlpha.size();
			iPairedSamples = lstBs.size();
		}
	}
	
	public double perimeterRegional(){
		return this.dRegionalPerimeterTotal;
	}
	
	public double areaRegional(){
		return this.dRegionalAreaTotal;
	}
	
	public double areaGlobal(){
		return this.dGlobalAreaTotal;
	}
	
	public double areaGlobalScaled(){
		return this.dGlobalScaledAreaTotal;
	}
	
	public double perimeterGlobal(){
		return this.dGlobalPerimeterTotal;
	}
	
	public double perimeterGlobalScaled(){
		return this.dGlobalScaledPerimeterTotal;
	}
	
	public double beta_j(){
		return dBj;
	}
	
	public double beta_s(){
		return dBs;
	}
	
	public double beta_w(){
		return dBw;
	}
	
	public double alpha(){
		return dAlpha;
	}
	
	public int numberOfSpecies(){
		return iNumberOfSpecies;
	}
	
	public int numberOfPairedSamples(){
		return iPairedSamples;
	}
	
	public int numberOfUnpairedSamples(){
		return iUnpairedSamples;
	}
	
	public SphericalCapEarth capGlobal(){
		return capGlobal;
	}
}
