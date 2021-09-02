package edu.ucsf.sdm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.ucsf.io.BiomIO;

/**
 * Object for storing projection data when projecting differences
 * @author jladau
 */

public class ProjectionData_Differences extends ProjectionData{

	private String sResponseDifferenceTransform;
	
	public ProjectionData_Differences(ProjectionData prd1, String sResponseDifferenceTransform){
		super(prd1);
		this.sResponseDifferenceTransform = sResponseDifferenceTransform;
	}
	
	public ProjectionDatum getProjectionData(ProjectionPoint ppt1) throws Exception{
		
		//map1 = first values for prediction
		//map2 = second values for prediction
		//pdm1 = output
		//ppd1 = coerced projection point
		
		ProjectionPoint_Differences ppd1;
		HashMap<String,Double> map1;
		HashMap<String,Double> map2;
		ProjectionDatum pdm1;
		
		ppd1 = (ProjectionPoint_Differences) ppt1;
		map1 = super.getProjectionData(ppd1.ppt1);
		map2 = super.getProjectionData(ppd1.ppt2);	
		pdm1 = new ProjectionDatum(map1.size());
		for(String s:map1.keySet()){
			pdm1.put(
					s, 
					PredictorTransformDifferences.apply(ppd1.ppt1.sID, ppd1.ppt1.sDateAlias, map1.get(s), ppd1.ppt2.sID, ppd1.ppt2.sDateAlias, map2.get(s), sResponseDifferenceTransform));
		}
		return pdm1;
	}
	
	public ArrayList<ProjectionPoint> loadAllPossibleProjectionPoints(String sResponseVariable, BiomIO bio1){
		
		//lstOut = output
		//lst1 = list of all sample-date combinations
		//setSamples = set of samples
		
		HashSet<String> setSamples;
		ArrayList<ProjectionPoint> lstOut;
		ArrayList<String[]> lst1;
		
		setSamples = trn1.getNonNanSamples(sResponseVariable);
		lst1 = new ArrayList<String[]>(getProjectionDateAliases().size()*setSamples.size());
		for(String s:getProjectionDateAliases()){
			for(String t:setSamples){
				lst1.add(new String[]{t,s});
			}
		}
		lstOut = new ArrayList<ProjectionPoint>(lst1.size()*lst1.size());
		for(int i=0;i<lst1.size();i++){
			for(int k=0;k<=i;k++){
				lstOut.add(new ProjectionPoint_Differences(		
						Double.parseDouble(bio1.axsSample.getMetadata(lst1.get(i)[0]).get("latitude")), 
						Double.parseDouble(bio1.axsSample.getMetadata(lst1.get(i)[0]).get("longitude")), 
						lst1.get(i)[1],
						Double.parseDouble(bio1.axsSample.getMetadata(lst1.get(k)[0]).get("latitude")), 
						Double.parseDouble(bio1.axsSample.getMetadata(lst1.get(k)[0]).get("longitude")), 
						lst1.get(k)[1],
						dProjectionVert,
						lst1.get(i)[0],
						lst1.get(k)[0]));
			}
		}
		return lstOut;
	}

	public class ProjectionPoint_Differences extends ProjectionPoint{
		
		public ProjectionPoint ppt1;
		public ProjectionPoint ppt2;
		
		public ProjectionPoint_Differences(double dLat1, double dLon1, String sDateAlias1, double dLat2, double dLon2, String sDateAlias2, double dVert, String sID1, String sID2){
			sID = PredictorTransformDifferences.getOrderedID(sID1, sDateAlias1, sID2, sDateAlias2);
			sDateAlias = PredictorTransformDifferences.getOrderedDates(sID1, sDateAlias1, sID2, sDateAlias2);
			ppt1 = new ProjectionPoint(dLat1,dLon1,sDateAlias1,dVert,sID1);
			ppt2 = new ProjectionPoint(dLat2,dLon2,sDateAlias2,dVert,sID2);
		}
	}
}