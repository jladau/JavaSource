package edu.ucsf.Geospatial.CleanLatitudeLongitudeData;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Cleans errors from latitude/longitude data
 * @author jladau
 *
 */

public class CleanLatitudeLongitudeDataLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//rgsHeaders = latitude and longitude headers
		//d1 = current value
		
		double d1;
		ArgumentIO arg1;
		DataIO dat1;
		String rgsHeaders[];
		
		//initializing
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//looping through data
		rgsHeaders = new String[]{"LATITUDE","LONGITUDE"};
		for(int i=1;i<dat1.iRows;i++){
			for(String s:rgsHeaders){
				if(dat1.hasHeader(s)){
					try{
						d1 = Double.parseDouble(dat1.getString(i, s));
						if(s.equals("LATITUDE") && Math.abs(d1)>90){
							throw new NumberFormatException();
						}
						if(s.equals("LONGITUDE") && Math.abs(d1)>180){
							throw new NumberFormatException();
						}
					}catch(NumberFormatException e){
						dat1.setString(i, s, "na");
					}
				}
			}
		}
		
		
		//outputting results
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done");
	}
	
	
}
