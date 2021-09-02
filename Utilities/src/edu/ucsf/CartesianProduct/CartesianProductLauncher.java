package edu.ucsf.CartesianProduct;

import java.util.ArrayList;

import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class CartesianProductLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = first data set
		//dat2 = second data set
		//lstOut = output
		//b1 = flag for whether output has been initialized
		
		ArgumentIO arg1;
		DataIO dat1;
		DataIO dat2;
		ArrayList<String> lstOut;
		boolean b1;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath1"));
		dat2 = new DataIO(arg1.getValueString("sDataPath2"));		
		b1 = false;
		
		lstOut = new ArrayList<String>(100000);
		lstOut.add(Joiner.on(",").join(dat1.getRow(0)) + "," + Joiner.on(",").join(dat2.getRow(0)));
		for(int i=1;i<dat1.iRows;i++){
			for(int k=1;k<dat2.iRows;k++){
				lstOut.add(Joiner.on(",").join(dat1.getRow(i)) + "," + Joiner.on(",").join(dat2.getRow(k)));
				if(lstOut.size()==100000){
					DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"), b1);
					b1=true;
					lstOut = new ArrayList<String>(100000);
				}
			}
		}
		if(lstOut.size()>=1){
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"), b1);
		}
		System.out.println("Done.");
		
		
	}
	
	
}
