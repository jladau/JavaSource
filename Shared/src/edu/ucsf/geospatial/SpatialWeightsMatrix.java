package edu.ucsf.geospatial;

import java.util.ArrayList;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Range;
import static com.google.common.math.DoubleMath.*;

//import static edu.ucsf.EarthGeometry.*;
import static java.lang.Math.*;
import edu.ucsf.base.Graph;
import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.io.BiomIO;

//TODO clean up names of functions used for calculating weights

/**
 * Spatial weights matrix object
 * @author jladau
 */
public class SpatialWeightsMatrix extends Graph{

	//iNonzeroWeights = number of non-zero weights
	//dFractionWithNeighbors = fraction of observations with neighbors
	//ert1 = EarthGeometry object
	//sMode = mode
	
	private EarthGeometry ert1;
	public int iNonzeroWeights;
	public double dFractionWithNeighbors;
	private String sMode;
	
	/**Weighting scheme to use. Can be either "inverse" or "binary."**/
	public String sWeight;
	
	/**Range of time differences to be included in neighborhoods.**/
	public Range<Double> rngTimeDifferences;
	
	/**Range of directions to be included in neighborhoods.**/
	public Range<Double> rngDirections;
	
	/**Range of distances to be included in neighborhoods.**/
	public Range<Double> rngDistances;
	
	/**
	 * Constructor
	 * @param sWeight Weighting scheme: "inverse" or "binary"
	 * @param bBivariate True of data are bivariate; false otherwise
	 * @param sMode Type of geometry: "latitude-longitude" or "euclidean"
	 * @throws Exception 
	 */	
	public SpatialWeightsMatrix(BiomIO bio1, Range<Double> rngDistances, Range<Double> rngDirections, Range<Double> rngTimeDifferences, String sWeight, String sMode, boolean bSaveAllEdges) throws Exception{
		
		//initializing geometry object
		ert1 = new EarthGeometry();
		
		//saving weighting scheme
		this.sWeight=sWeight;
		
		//saving intervals
		this.rngDistances = rngDistances;
		this.rngDirections = rngDirections;
		this.rngTimeDifferences = rngTimeDifferences;
		
		//saving mode
		this.sMode = sMode;
		
		//loading graph
		this.loadGraph(bio1, bSaveAllEdges);
		
		//loading weights
		this.loadWeights();
		
		//validating matrix
		this.validateMatrix();
	}
	
	/**
	 * Gets weight for specified edge.
	 * @param iEdgeID ID of edge
	 * @return Weight for edge.
	 */
	public double getWeight(GraphEdge edg1){
		
		if(edg1==null){
			return 0.;
		}else{
			return edg1.getDouble("dWeight");
		}
	}

	
	/**
	 * Gets weights matrix in table format
	 * @return Weights matrix
	 */
	public ArrayTable<GraphVertex,GraphVertex,Double> getTable(){
		
		//tbl1 = output
		
		ArrayTable<GraphVertex,GraphVertex,Double> tbl1;
		
		tbl1 = ArrayTable.create(this.getVertices(),this.getVertices());
		for(GraphVertex vtxStart:this.getVertices()){
			for(GraphVertex vtxEnd:this.getVertices()){
				if(containsEdge(vtxStart.iID,vtxEnd.iID)){
					tbl1.put(vtxStart, vtxEnd, getWeight(getEdge(vtxStart.iID,vtxEnd.iID)));
				}else{
					tbl1.put(vtxStart, vtxEnd, 0.);
				}
				
			}
		}
		return tbl1;
	}
	
	/**
	 * Gets list of all weights.
	 * @return List of all weights.
	 */
	public ArrayList<Double> getWeights(){
		return getEdgeProperties("dWeight");
	}

	/**
	 * Gets list of all location values.
	 * @return List of all location values.
	 */
	//public ArrayList<Double> getLocationValues(){
	//	return getVertexProperties("dValue");
	//}

	/**
	 * Gets value for specified vertex.
	 * @param iVertexID id of vertex.
	 * @return Value for vertex.
	 */
	//public double getLocationValue(int iVertexID){
	//	return this.getVertex(iVertexID).getDouble("dValue");
	//}

	/**
	 * Loads graph
	 * @param bio1 BIOM object from which to load graph
	 * @param sMode Type of graph: "latitude-longitude" or "euclidean"
	 * @param bSaveAllEdges Flag for whether to save all edges
	 */
	private void loadGraph(BiomIO bio1, boolean bSaveAllEdges){
		
		//vtx1 = current vertex being added to graph
		//edg1 = current candidate edge
		//rgs1 = current date in split format
		
		GraphVertex vtx1;
		GraphEdge_SpatioTemporal edg1;
		String rgs1[];
		
		//loading graph with weights and values
		for(String s:bio1.axsSample.getIDs()){
			
			//loading vertex
			vtx1 = new GraphVertex(bio1.axsSample.getIndex(s),s);
			if(sMode.equals("latitude-longitude")){
				vtx1.put("dX", Double.parseDouble(bio1.axsSample.getMetadata(s).get("longitude")));
				vtx1.put("dY", Double.parseDouble(bio1.axsSample.getMetadata(s).get("latitude")));
			}else if(sMode.equals("euclidean")){
				vtx1.put("dX", Double.parseDouble(bio1.axsSample.getMetadata(s).get("x")));
				vtx1.put("dY", Double.parseDouble(bio1.axsSample.getMetadata(s).get("y")));
			}
			try{
				rgs1 = bio1.axsSample.getMetadata(s).get("datetime").substring(0, 10).split("-");
				vtx1.put("timDate", new LocalDate(Integer.parseInt(rgs1[0]),Integer.parseInt(rgs1[1]),Integer.parseInt(rgs1[2])));
			}catch(Exception e){
				vtx1.put("timDate", new LocalDate(9999,9,9));				
			}
		
			this.addVertex(vtx1);
		}
		
		//loading edges
		for(int i=0;i<bio1.axsSample.size();i++){
			for(int j=0;j<bio1.axsSample.size();j++){
				
				//loading edge and checking if non-zero weight
				edg1=new GraphEdge_SpatioTemporal(this.getVertex(i), this.getVertex(j));
				if(bSaveAllEdges==true || calculateWeight(edg1)!=0){
					this.addEdge(edg1);
				}
			}
		}
	}
	
	/**
	 * Loads weights (note: values of minimum and maximum distances, bearings, and time differences should be added first).
	 * @throws Exception 
	 */
	private void loadWeights() throws Exception{
		
		//checking if spatial neighborhood specified
		if(rngTimeDifferences.encloses(Range.closed(0.,12.))){
			if(!rngDistances.hasUpperBound()){
				if(rngDistances.encloses(Range.closed(0.,360.))){
					throw new Exception("SpatialWeightsMatrix.java: no neighborhood specified.");
				}
			}
		}
		
		//initializing distances
		this.initializeWeightsMatrix();
		
		//saving weights
		for(GraphEdge edg1:getEdges()){
			edg1.put("dWeight", calculateWeight2(edg1));
		}
	}
	
	/**
	 * Validates matrix: checks that row sums are 1, entries are non-zero only where distances are within bounds, and if bivariate then nonzero entries are only where there are different categories 
	 * @throws Exception 
	 */
	private void validateMatrix() throws Exception{
		
		//mapRowSum(iVertexID) = row sum for current vertex
		//dDistance = current distance
		//dWeight = current weight
		
		HashMap_AdditiveDouble<Integer> mapRowSum;
		double dDistance;
		double dWeight;
		
		mapRowSum = new HashMap_AdditiveDouble<Integer>();
		for(GraphEdge edg1:getEdges()){
			mapRowSum.putSum(edg1.vtxStart.iID, edg1.getDouble("dWeight"));
			dDistance=edg1.getDouble("dLength");
			dWeight=edg1.getDouble("dWeight");
			
			//Checking categories and bounds
			if(!rngDistances.contains(dDistance)){
				if(dWeight!=0){
					throw new Exception("SpatialWeightsMatrix.java: non-zero weight outside of bounds.");
				}
			}
		}
			
		//checking row sums
		for(int i:mapRowSum.keySet()){
			if(mapRowSum.get(i)!=0 && !fuzzyEquals(mapRowSum.get(i),1.,0.00000001)){
				throw new Exception("SpatialWeightsMatrix.java: rows do not sum to 1.");
			}
		}
	}

	/**
	 * Calculates weight for specified edge
	 * @param graphEdge Edge to calculated weight for
	 * @return Weight
	 */
	private double calculateWeight(GraphEdge edg1){
		//if(edg1==null){
		//	return 0;
		//}else{
			return calculateWeightDistance(edg1)*calculateWeightDirection(edg1)*calculateWeightTime(edg1);
		//}
	}

	/**
	 * Gets weight
	 * @param graphEdge Edge to consider
	 * @return Weight
	 */
	private double calculateWeight2(GraphEdge edg1){
		if(edg1.vtxStart.getDouble("dRowSumWeight")==0){
			return 0;
		}else{
			return calculateWeight(edg1)/edg1.vtxStart.getDouble("dRowSumWeight");
		}
	}
	
	/**
	 * Initializes weights matrix.
	 */
	private void initializeWeightsMatrix(){
		
		//dWeight = current weight
		//lstEdgesToRemove = list of edges to remove
		
		double dWeight;
		ArrayList<GraphEdge> lstEdgesToRemove;
		
		//initializing arrays
		iNonzeroWeights=0;
		dFractionWithNeighbors=0;
		
		//loading row sums
		for(GraphVertex vtx1:getVertices()){
			vtx1.put("dRowSumWeight", 0.);
		}
		
		//initializing list of edges to remove
		lstEdgesToRemove = new ArrayList<GraphEdge>(size());
		
		//looping through edges
		for(GraphEdge edg1:getEdges()){
				
			//loading weight
			dWeight=this.calculateWeight(edg1);
			
			//checking if weight is non-zero
			if(dWeight>0){
				edg1.vtxStart.putSum("dRowSumWeight", dWeight);
				iNonzeroWeights++;
			}else{
				lstEdgesToRemove.add(edg1);
			}
		}
		
		//removing edges with zero weight
		for(int k=0;k<lstEdgesToRemove.size();k++){
			
			//TODO need to check that no edges are being removed
			System.out.println("ERROR: There shouldn't be any edges to remove!");
			
			//this.removeEdge(lstEdgesToRemove.get(k));
		}
		
		//loading fraction with neighbors
		for(GraphVertex vtx1:getVertices()){
			if(vtx1.getDouble("dRowSumWeight")>0){
				dFractionWithNeighbors++;
			}
		}
		dFractionWithNeighbors=dFractionWithNeighbors/((double) order());
	}
	
	/**
	 * Calculates weight based on distance
	 * @param edg1 Edge for which to calculate weight
	 * @return Weight
	 */
	private double calculateWeightDistance(GraphEdge edg1){
		
		if(checkCategoriesAndIDs(edg1) || !rngDistances.contains(edg1.getDouble("dLength"))){
			return 0;
		}else{	
			if(sWeight.equals("inverse")){	
				return 1./(edg1.getDouble("dLength")+1.);
			}else if(sWeight.equals("binary")){
				return 1;
			}else{
				return -9999;
			}
		}
	}
	
	/**
	 * Calculates weight based on direction
	 * @param edg1 Edge for which to calculate weight
	 * @return Weight
	 */
	private double calculateWeightDirection(GraphEdge edg1){
		
		if(checkCategoriesAndIDs(edg1)){
			return 0;
		}else if((edg1.getDouble("dInitialBearing") == -9999 && (rngDirections.lowerEndpoint()>0 || rngDirections.upperEndpoint()<360))){
			return 0;
		}else if(EarthGeometry.checkBearingInRange(edg1.getDouble("dInitialBearing"), rngDirections.lowerEndpoint(), rngDirections.upperEndpoint())==false){
			return 0;
		}else{	
		
			//note: only binary time weights currently supported 
			return 1;
		}
	}
	
	/**
	 * Calculates weight based on time
	 * @param edg1 Edge for which to calculate weight
	 * @return Weight
	 */
	private double calculateWeightTime(GraphEdge edg1){
		
		if(checkCategoriesAndIDs(edg1) || !rngTimeDifferences.contains(edg1.getDouble("dTimeDifference"))){
			return 0;
		}else{	
			
			//note: only binary time weights currently supported 
			return 1;
		}
	}
	
	/**
	 * Checks categories and IDs
	 * @param edg1 Edge to check
	 * @return True if passed; false otherwise.
	 */
	private boolean checkCategoriesAndIDs(GraphEdge edg1){
		if(edg1.vtxStart.iID == edg1.vtxEnd.iID){
			return true;
		}else{
			return false;
		}
	}
	
	private class GraphEdge_SpatioTemporal extends GraphEdge{

		/**
		 * Constructor
		 * @param iID Edge ID
		 */
		private GraphEdge_SpatioTemporal(GraphVertex vtxStart, GraphVertex vtxEnd){
			super(vtxStart,vtxEnd);
			this.put("dLength", this.findDistance(vtxStart.getDouble("dX"), vtxStart.getDouble("dY"), vtxEnd.getDouble("dX"), vtxEnd.getDouble("dY")));
			this.put("dInitialBearing", this.findInitialBearing(vtxStart.getDouble("dX"), vtxStart.getDouble("dY"), vtxEnd.getDouble("dX"), vtxEnd.getDouble("dY")));
			this.put("dTimeDifference", this.findTimeDifference());
		}
		
		/**
		 * Finds distance between two points
		 */
		private double findDistance(double dX1, double dY1, double dX2, double dY2){
			
			if(sMode.equals("latitude-longitude")){
				return ert1.orthodromicDistanceWGS84(dY1, dX1, dY2, dX2);
			}else if(sMode.equals("euclidean")){
				return sqrt(pow(dX2-dX1,2.)+pow(dY2-dY1,2));
			}else{
				return Double.NaN;
			}
		}
		
		/**
		 * Finds initial bearing between two points
		 */
		private double findInitialBearing(double dXStart, double dYStart, double dXEnd, double dYEnd){
			
			//d1 = initial angle
			
			double d1;
			
			if(sMode.equals("latitude-longitude")){
				return ert1.initialGeodesicBearingWGS84(dYStart, dXStart, dYEnd, dXEnd);
			}else if(sMode.equals("euclidean")){
				d1=atan2(dYStart-dYEnd, dXStart-dXEnd);
				d1=d1*EarthGeometry.RAD_TO_DEG;
				d1=d1-90.;
				if(d1<0){
					d1+=360.;
				}
				return d1;
			}else{
				return Double.NaN;
			}
		}
		
		private double findTimeDifference(){
			return (double) abs(Days.daysBetween(vtxEnd.getTime("timDate"), vtxStart.getTime("timDate")).getDays());
		}
	}
}
