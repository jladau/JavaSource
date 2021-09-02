package edu.ucsf.geospatial;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Range;

import edu.ucsf.base.Graph.GraphVertex;
import edu.ucsf.io.BiomIO;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Spatial weights matrix object
 * @author jladau
 */
public class SpatialWeightsMatrixTest{

	/**List of sample latitudes (grid is constructed)**/
	private double rgdLat[];
	
	/**List of sample longitudes (grid is constructed)**/
	private double rgdLon[];
	
	/**Array giving relative abundances for OTUs**/
	private double rgdAbund[][];
	
	/**Table giving weights**/
	private HashBasedTable<Integer,Integer,Double> tblWeight;
	
	/**Spatial weights matrix**/
	private SpatialWeightsMatrix spw1;
	
	/**Points**/
	private double rgdPoints[][];
	
	/**BIOM object.**/
	private BiomIO bio1;
	
	public SpatialWeightsMatrixTest(){
		initialize();
	}
	
	private void initialize(){
			
		//ert1 = earth geometry object
		//dThreshold = threshold distance
		//ranDist = distances range
		//ranTime = time range
		//ranDirect = directions range
		//i1 = counter
		//rgdSum = row sums
		
		double dThreshold;
		EarthGeometry ert1;
		Range<Double> ranDist;
		Range<Double> ranTime;
		Range<Double> ranDirect;
		int i1;
		double rgdSum[];
		
		//initializing mocked data
		rgdLat = new double[]{45.,46.,47.,48.};
		rgdLon = new double[]{-110.,-109.,-108.,-107.};
		rgdPoints = new double[rgdLat.length*rgdLat.length][2];
		i1 = 0;
		for(int i=0;i<rgdLat.length;i++){
			for(int j=0;j<rgdLat.length;j++){
				rgdPoints[i1][0]=rgdLat[i];
				rgdPoints[i1][1]=rgdLon[j];
				i1++;
			}
		}
		
		rgdAbund = new double[2][rgdPoints.length];
		tblWeight = HashBasedTable.create(4,4);
		dThreshold = 150.;
		
		ert1 = new EarthGeometry();
		rgdSum = new double[rgdPoints.length];
		for(int i=0;i<rgdPoints.length;i++){
			for(int j=0;j<rgdPoints.length;j++){
				if(i!=j && ert1.orthodromicDistanceWGS84(rgdPoints[i][0], rgdPoints[i][1], rgdPoints[j][0], rgdPoints[j][1])<dThreshold){
					tblWeight.put(i, j, 1.);
					rgdSum[i]+=1.;
				}else{
					tblWeight.put(i, j, 0.);
				}
			}
			rgdAbund[0][i] = ((double) i)/20.;
			rgdAbund[1][i] = (((double) i) % 4.)/4.;
		}	
		
		for(int i=0;i<rgdPoints.length;i++){
			for(int j=0;j<rgdPoints.length;j++){
				if(rgdSum[i]>0){
					tblWeight.put(i, j, tblWeight.get(i, j)/rgdSum[i]);
				}
			}
		}
		
		//loading spatial weights object from file
		ranDist = Range.closed(0.,dThreshold);
		ranTime = Range.closed(0.,12.);
		ranDirect = Range.closed(0.,360.);
		bio1 = new BiomIO("/home/jladau/Desktop/Data/Microbial_Community_Samples/ValidationData.NA.NA.Ladau.biom");
		
		try{
			spw1 = new SpatialWeightsMatrix(bio1,ranDist,ranDirect,ranTime,"binary","latitude-longitude",false);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void calculateWeight_WeightsCalculated_WeightsCorrect(){
		for(Integer i:tblWeight.rowKeySet()){
			for(Integer j:tblWeight.columnKeySet()){
				assertEquals(tblWeight.get(i, j),spw1.getWeight(spw1.getEdge(i,j)),0.0000001);
			}
		}
	}
	
	@Test
	public void getTable_TableGotten_TableCorrect(){
		
		//tbl1 = table
		//rgd1 = table array format
		
		Double[][] rgd1;
		ArrayTable<GraphVertex,GraphVertex,Double> tbl1;
		
		tbl1 = spw1.getTable();
		rgd1 = tbl1.toArray(Double.class);
		for(SpatialWeightsMatrix.GraphEdge edg1:spw1.getEdges()){
			assertEquals(spw1.getWeight(edg1),tbl1.get(edg1.vtxStart, edg1.vtxEnd),0.000001);
		}
		assertEquals(tbl1.rowKeySet().size(),spw1.getVertices().size());
		for(int i=0;i<tbl1.rowKeyList().size();i++){
			for(int j=0;j<tbl1.columnKeyList().size();j++){
				if(spw1.containsEdge(tbl1.rowKeyList().get(i).iID, tbl1.columnKeyList().get(j).iID)){
					assertEquals(rgd1[i][j], spw1.getEdge(tbl1.rowKeyList().get(i).iID, tbl1.columnKeyList().get(j).iID).getDouble("dWeight"),0.000001);	
					assertEquals(tbl1.get(tbl1.rowKeyList().get(i), tbl1.columnKeyList().get(j)), spw1.getEdge(tbl1.rowKeyList().get(i).iID, tbl1.columnKeyList().get(j).iID).getDouble("dWeight"),0.000001);
				}else{
					assertEquals(rgd1[i][j],0.,0.0000001);
					assertEquals(tbl1.get(tbl1.rowKeyList().get(i), tbl1.columnKeyList().get(j)), 0,0.000001);
				}
			}
		}
	}
}
