package edu.ucsf.HasseDistanceDiagram;

import java.util.ArrayList;
import java.util.HashMap;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class HasseDistanceDiagramLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = initial conditions, vertices in order of coordinates to be found
		//lstOut = output
		//map1 = map from vertex ids to hasse vertices (reference)
		//map2 = map from pairs of vertex ids to edges
		//hvt2 = current vertex
		//edg1 = current edge
		//lstPath = path between current pair of vertices
		//hgr1 = hasse graph
		
		HasseGraph hgr1;
		HasseEdge edg1;
		DataIO dat1;
		ArgumentIO arg1;
		HasseVertex hvt2;
		ArrayList<String> lstOut;
		ArrayList<Double[]> lstPath;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("ID,X,Y");
		hgr1 = new HasseGraph(dat1.iRows);
		
		//looping through pairs of vertices
		hgr1.putVertex(new HasseVertex(
				0.,
				0.,
				dat1.getString(1, "START_VERTEX")));
		for(int i=1;i<dat1.iRows;i++){
			if(!dat1.getString(i, "DISTANCE").equals("none")){
				hvt2 = new HasseVertex(
					hgr1.getVertex(dat1.getString(i, "START_VERTEX")), 
					dat1.getDouble(i, "DISTANCE"), 
					dat1.getDouble(i, "ANGLE"), 
					dat1.getString(i, "END_VERTEX"));
				hgr1.putEdge(
						dat1.getString(i, "START_VERTEX"), 
						hvt2, 
						dat1.getDouble(i, "PATH_LENGTH"),
						dat1.getString(i, "EDGE_ID"));
			}else{
				hgr1.putEdge(
					dat1.getString(i, "START_VERTEX"), 
					dat1.getString(i, "END_VERTEX"), 
					dat1.getDouble(i, "PATH_LENGTH"),
					dat1.getString(i, "EDGE_ID"));
			}
		}
		hgr1.loadPaths();
		
		//DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}