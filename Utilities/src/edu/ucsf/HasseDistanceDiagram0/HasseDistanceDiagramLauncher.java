package edu.ucsf.HasseDistanceDiagram0;

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
		//hvt1 = first reference vertex
		//hvt2 = second reference vertex
		//hvt0 = current vertex
		
		DataIO dat1;
		ArgumentIO arg1;
		HasseVertex hvt0;
		HasseVertex hvt1;
		HasseVertex hvt2;
		ArrayList<String> lstOut;
		HashMap<String,HasseVertex> map1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("ID,X,Y");
		map1 = new HashMap<String,HasseVertex>(dat1.iRows);
		
		//looping through pairs of vertices
		map1.put(dat1.getString(1, "ID"), new HasseVertex(
				dat1.getDouble(1, "X"),
				dat1.getDouble(1, "Y"),
				dat1.getString(1, "ID")));
		lstOut.add(dat1.getString(1, "ID") + "," + dat1.getString(1, "X") + "," + dat1.getString(1, "Y"));
		
		for(int i=2;i<dat1.iRows;i++){
			hvt1 = map1.get(dat1.getString(i, "REFERENCE_VERTEX_1"));
			if(!dat1.getString(i, "REFERENCE_VERTEX_2").equals("none")){
				hvt2 = map1.get(dat1.getString(i, "REFERENCE_VERTEX_2"));
				hvt0 = new HasseVertex(
						hvt1, 
						hvt2, 
						dat1.getDouble(i, "DISTANCE_VERTEX_1"), 
						dat1.getDouble(i, "DISTANCE_VERTEX_2"), 
						dat1.getString(i, "MOVE_DIRECTION"), 
						dat1.getString(i, "ID"));
				hvt0.move(arg1.getValueDouble("dThreshold"), 1000);
			}else{
				hvt0 = new HasseVertex(
						hvt1, 
						dat1.getDouble(i, "DISTANCE_VERTEX_1"), 
						dat1.getDouble(i, "ANGLE_VERTEX_1"), 
						dat1.getString(i, "ID"));
			}
			lstOut.add(hvt0.id() + "," + hvt0.x() + "," + hvt0.y());
			map1.put(hvt0.id(), new HasseVertex(
					hvt0.x(),
					hvt0.y(),
					hvt0.id()));
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}