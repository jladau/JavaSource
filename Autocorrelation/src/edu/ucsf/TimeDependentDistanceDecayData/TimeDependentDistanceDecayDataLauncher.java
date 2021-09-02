package edu.ucsf.TimeDependentDistanceDecayData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;

import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Calculates distance decay across a range of times, where coordinates vary by time (e.g., due to plate motions)
 * @author jladau
 *
 */

public class TimeDependentDistanceDecayDataLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//datCoord = coordinates data file
		//datBeta = beta-diversity data file
		//tblLat = returns latitude for given sample, time
		//tblLon = returns longitude for given sample, time
		//setTimes = set of times
		//rgd1 = latitude1, longitude1, latitude2, longitude2
		//sSample1 = first sample
		//sSample2 = second sample
		//geo1 = earth geometry object
		//lstOut = output
		//mapLastKnownTime = last known time for given sample
		//sSampleCur = current sample
		//sTimeCur = current time
		//dDistance = current distance
		//lstDistances = distance data
		//dLat1 = current first latitude
		//dLat2 = current second latitude
		//dLon1 = current first longitude
		//dLon2 = current second longitude
		
		ArrayList<StringBuilder> lstDistances;
		double dDistance = Double.NaN;
		String sSampleCur;
		String sTimeCur;
		HashMap<String,String> mapLastKnownTime;
		EarthGeometry geo1;
		String sSample1;
		String sSample2;
		ArgumentIO arg1;
		DataIO datCoord;
		DataIO datBeta;
		HashBasedTable<String,String,Double> tblLat;
		HashBasedTable<String,String,Double> tblLon;
		HashSet<String> setTimes;
		double[] rgd1;
		ArrayList<String> lstOut;
		ArrayList<String> lstOut2;
		double dLat1;
		double dLat2;
		double dLon1;
		double dLon2;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		datCoord = new DataIO(arg1.getValueString("sCoordinateDataPath"));
		datBeta = new DataIO(arg1.getValueString("sBetaDiversityPath"));
		geo1 = new EarthGeometry();
		lstOut = new ArrayList<String>(500);
		lstOut.add("TIME,CORRELATION");
		lstOut2 = new ArrayList<String>(datBeta.iRows);
		
		//loading tables with latitudes and longitudes and beta-diversity measures, and times
		tblLat = HashBasedTable.create(5000, 200);
		tblLon = HashBasedTable.create(5000, 200);
		setTimes = new HashSet<String>(200);
		mapLastKnownTime = new HashMap<String,String>(1000);
		for(int i=1;i<datCoord.iRows;i++){
			sSampleCur = datCoord.getString(i, "SAMPLE");
			sTimeCur = datCoord.getString(i, "TIME");
			tblLat.put(sSampleCur, sTimeCur, datCoord.getDouble(i, "LATITUDE"));
			tblLon.put(sSampleCur, sTimeCur, datCoord.getDouble(i, "LONGITUDE"));
			setTimes.add(sTimeCur);
			if(!mapLastKnownTime.containsKey(sSampleCur)){
				mapLastKnownTime.put(sSampleCur,sTimeCur);
			}else{
				if(Double.parseDouble(sTimeCur)>Double.parseDouble(mapLastKnownTime.get(sSampleCur))){
					mapLastKnownTime.put(sSampleCur,sTimeCur);		
				}
			}
		}
		
		//adding last known locations
		for(String sSample:tblLat.rowKeySet()){
			for(String sTime:setTimes){
				if(!tblLat.contains(sSample, sTime)){
					//tblLat.put(sSample, sTime, tblLat.get(sSample, mapLastKnownTime.get(sSample)));
					//tblLon.put(sSample, sTime, tblLon.get(sSample, mapLastKnownTime.get(sSample)));
					tblLat.put(sSample, sTime, Double.NaN);
					tblLon.put(sSample, sTime, Double.NaN);
				}
			}
		}
		
		//initializing distances output
		lstDistances = new ArrayList<StringBuilder>(datBeta.iRows);
		lstDistances.add(new StringBuilder());
		for(int i=0;i<datBeta.iRows;i++){
			lstDistances.add(new StringBuilder());
			lstDistances.get(i).append(Joiner.on(",").join(datBeta.getRow(i)));
		}
		
		//looping through dates
		for(String sTime:setTimes){
			
			//updating distances output
			lstDistances.get(0).append(",DISTANCE_" + Double.parseDouble(sTime));
			
			//looping through beta-diversity measurements
			for(int i=1;i<datBeta.iRows;i++){
				sSample1 = datBeta.getString(i, "SAMPLE_1");
				sSample2 = datBeta.getString(i, "SAMPLE_2");
				try{
					dLat1 = tblLat.get(sSample1, sTime);
					dLon1 = tblLon.get(sSample1, sTime);
					dLat2 = tblLat.get(sSample2, sTime);
					dLon2 = tblLon.get(sSample2, sTime);
				}catch(Exception e){
					lstDistances.get(i).append("fha7e8reaysdftyaui");
					continue;
				}
				if(Double.isNaN(dLat1) || Double.isNaN(dLon1) || Double.isNaN(dLat2) || Double.isNaN(dLon2)){
					lstDistances.get(i).append(",0");
				}else{	
					try{
						rgd1 = new double[]{dLat1, dLon1, dLat2, dLon2};
						dDistance=geo1.orthodromicDistanceWGS84(rgd1[0], rgd1[1], rgd1[2], rgd1[3]);
						lstDistances.get(i).append("," + dDistance);
					}catch(Exception e){
						lstDistances.get(i).append(",0");
					}
				}
			}
		}
		
		//outputting results
		for(int i=0;i<lstDistances.size();i++){
			if(!lstDistances.get(i).toString().contains("fha7e8reaysdftyaui")){
				lstOut2.add(lstDistances.get(i).toString());
			}
		}
		DataIO.writeToFile(lstOut2, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}