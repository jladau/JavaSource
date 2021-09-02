package edu.ucsf.ShortenHeaders;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Shortens headers
 * @author jladau
 */


public class ShortenHeadersLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//iLength = length
		//s1 = current value
		
		ArgumentIO arg1;
		DataIO dat1;
		int iLength;
		String s1;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		iLength=arg1.getValueInt("iHeaderLength");
		for(int j=0;j<dat1.iCols;j++){
			s1 = dat1.getString(0, j);
			if(s1.length()>iLength){
				dat1.lstData.get(0).set(j, s1.substring(0, iLength));
			}
		}
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
