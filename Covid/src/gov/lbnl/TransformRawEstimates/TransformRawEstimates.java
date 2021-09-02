package gov.lbnl.TransformRawEstimates;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class TransformRawEstimates{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//datStartDate = data with start date (for time-slope)
		//datTimes = data with list of times to compute (for time-slope)
		//tbl1 = table from start dates, region ids to raw coefficient estimates
		//tbl2 = table from start dates, region ids to transformed coefficient estimates
		//dCurrentTime = current time
		//dStartTime = start time
		//iTime0 = time index for slope case
		//d1 = current candidate value
		//d2 = reciprocal of maximum possible coefficient value
		
		double d2;
		double d1;
		int iTime0;
		double dStartTime;
		double dCurrentTime;
		ArgumentIO arg1;
		DataIO dat1;
		DataIO datStartDate;
		DataIO datTimes;
		HashBasedTable<Integer,String,Double> tbl1;
		HashBasedTable<Integer,String,Double> tbl2;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		d2 = 1./arg1.getValueDouble("dMaximumValue");
		
		//loading table of raw coefficients
		tbl1 = HashBasedTable.create(300,300);
		for(int i=1;i<dat1.iRows;i++){
			tbl1.put(dat1.getInteger(i,"TIME"),dat1.getString(i,"REGION_ID"),dat1.getDouble(i,"COEFFICIENT_ESTIMATE"));
		}
		
		//selecting transformation
		if(tbl1.containsColumn("TIME_SLOPE")){
			datStartDate = new DataIO(arg1.getValueString("sStartDatePath"));
			dStartTime = datStartDate.getDouble(1,0);
			datTimes = new DataIO(arg1.getValueString("sTimesPath"));
			tbl2 = HashBasedTable.create(datTimes.iRows,tbl1.columnKeySet().size());
			iTime0=dat1.getInteger(1,"TIME");
			for(int i=1;i<datTimes.iRows;i++){
				dCurrentTime = datTimes.getDouble(i,"TIME");
				for(String s:tbl1.columnKeySet()) {
					if(!s.equals("TIME_SLOPE")){
						d1 = tbl1.get(iTime0,s) + d2 + tbl1.get(iTime0,"TIME_SLOPE")*(dCurrentTime-dStartTime);
						if(d1<d2){
							d1=d2;
						}
						d1=1./d1;
						tbl2.put(datTimes.getInteger(i,"TIME"),s,d1);
					}
				}
				tbl2.put(datTimes.getInteger(i,"TIME"),"TIME",dCurrentTime);
			}
			
		}else{
			tbl2 = HashBasedTable.create(tbl1.rowKeySet().size(),tbl1.columnKeySet().size());
			for(Integer i:tbl1.rowKeySet()){
				for(String s:tbl1.columnKeySet()){
					if(tbl1.contains(i,s)){
						tbl2.put(i,s,1./(tbl1.get(i,s)+d2));
					}else{
						tbl2.put(i,s,Double.NaN);
					}
				}
				tbl2.put(i,"TIME",1.*i);
			}
		}
		
		//outputting result
		DataIO.writeToFile(tbl2,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}