package edu.ucsf.ClusterIteratorSubsetOfFile;

import java.util.ArrayList;

import com.google.common.base.Joiner;

import edu.ucsf.base.ClusterIterator;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Loads subset of lines from file for use on a node on the cluster
 * @author jladau
 *
 */

public class ClusterIteratorSubsetOfFileLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = argument
		//dat1 = data
		//ctr1 = cluter iterator
		//lstOut = output
		
		ArgumentIO arg1;
		DataIO dat1;
		ClusterIterator ctr1;
		ArrayList<String> lstOut;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		ctr1 = new ClusterIterator(arg1.getValueInt("iTaskID"), arg1.getValueInt("iTotalTasks"));
		
		//outputting results
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add(Joiner.on(",").join(dat1.getRow(0)));
		for(int i=1;i<dat1.iRows;i++){
			ctr1.next();
			if(ctr1.bInclude==true){
				lstOut.add(Joiner.on(",").join(dat1.getRow(i)));
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}