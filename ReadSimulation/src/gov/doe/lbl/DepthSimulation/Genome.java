package gov.doe.lbl.DepthSimulation;

import java.util.ArrayList;
import java.util.HashMap;

public class Genome {

	
	/**List of base positions**/
	private ArrayList<BasePosition> lstBasePositions;
	
	/**Insertion**/
	private Insertion ins1;
	
	/**Type of genome**/
	private String sType;
	
	/**
	 * Constructor
	 * @param iBases Number of bases in genome
	 * @param ins1 Insertion
	 * @param sType "simulated" or "sequenced"
	 */
	public Genome(int iBases, Insertion ins1, String sType){
		lstBasePositions = new ArrayList<BasePosition>(iBases);
		this.ins1 = ins1;
		this.sType = sType;
		for(int i=0;i<iBases;i++){
			addBasePosition();
		}
	}

	/**
	 * Initializes a base position
	 * @param bInsertion Flag for whether position is an insertion
	 */	
	private void addBasePosition(){
		
		//i1 = current length of base positions array
		
		int i1;
		
		i1 = lstBasePositions.size();
		if(ins1.contains(i1)){
			lstBasePositions.add(new BasePosition(true));
		}else{
			lstBasePositions.add(new BasePosition(false));
		}
	}
	
	/**
	 * @return Insertion
	 */
	
	public Insertion insertion(){
		return ins1;
	}
	
	/**
	 * Check whether base position is within an insertion
	 * @param iIndex Index of base position
	 * @return True if within an insertion, false otherwise
	 */
	public boolean insertion(int iIndex){
		return lstBasePositions.get(iIndex).insertion();
	}
	
	
	/**
	 * Finds position relative to the start of the insertion
	 * @param iIndex Index
	 * @return Negative if before start, positive if after
	 */
	public int positionRelativeToInsertionStart(int iIndex){
		return iIndex - ins1.start();
	}
	
	
	/**
	 * Checks where a location is relative to the insertion
	 * @param iIndex Location to check
	 * @return "within", "before", or "after"
	 */
	public String positionRelativeToInsertion(int iIndex){
		return ins1.relativePosition(iIndex);
	}
	
	
	/**
	 * Finds the depth at the given index
	 * @param iIndex Index
	 */
	public int depth(int iIndex){
		return lstBasePositions.get(iIndex).depth();
	}
	
	/**
	 * Sets the number of reads at a given position
	 * @param iIndex Position
	 * @param iReadStarts Number of reads
	 */
	public void setReadStarts(int iIndex, int iReadStarts){
		lstBasePositions.get(iIndex).putReadStarts(iReadStarts);
	}
	
	/**
	 * Returns the number of reads starting at the specified position
	 * @param iIndex Position
	 */
	public int readStarts(int iIndex){
		return lstBasePositions.get(iIndex).readStarts();
	}
	
	/**Returns the number of bases in the current genome**/
	public int size(){
		return lstBasePositions.size();
	}
	
	/**
	 * Loads depths once number of reads at each location is loaded
	 * @param iReadLength Length of reads
	 */
	public void loadDepths(int iReadLength){
		
		//i1 = current depth
		//i2 = current start index
		
		int i1;
		int i2;
		
		for(int i=0;i<lstBasePositions.size();i++){
			i1 = 0;
			i2 = Math.max(0, i-iReadLength+1);
			for(int j=i2;j<=i;j++){
				i1+=lstBasePositions.get(j).readStarts();
			}
			lstBasePositions.get(i).putDepth(i1);
		}
	}
	
	/**
	 * Returns the distance from the insertion
	 * @param iIndex Location to check
	 * @return Distance in base pairs
	 */
	public int distanceFromInsertion(int iIndex){
		return ins1.distance(iIndex);
	}
	
	/**
	 * 
	 * @param iTrimDistance
	 * @return
	 */
	public ArrayList<String> print(int iTrimDistance, boolean bHeader){
		
		//lstOut = output
		
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>(lstBasePositions.size()+1);
		if(bHeader==true){
			lstOut.add("TYPE,BASE_POSITION,BASE_POSITION_RELATIVE,LOCATION_RELATIVE_TO_INSERTION,READ_STARTS,DEPTH");
		}
		for(int i=0;i<lstBasePositions.size();i++){
			if(distanceFromInsertion(i)<=iTrimDistance){
				lstOut.add(this.sType + "," + i + "," + this.positionRelativeToInsertionStart(i) + "," + this.positionRelativeToInsertion(i) + "," + readStarts(i) + "," + depth(i));
			}
		}
		return lstOut;
	}
	
	/**
	 * Maps current genome to a simulated genome
	 * @param genSimulated
	 * @return Simulated genome
	 */
	
	public Genome mapTo0(Genome genSimulated){
	
		//i1 = counter for current genome
		//iSimulated = counter for simulated genome
		
		int i1;
		int iSimulated;
		
		if(this.size()>genSimulated.size()){
			iSimulated = 0;
			for(int i=0;i<this.size();i++){
				if(this.insertion(i)!=genSimulated.insertion(iSimulated)){
					continue;
				}else{
					genSimulated.setReadStarts(iSimulated, this.readStarts(i));
					iSimulated++;
				}
			}
		}else if(this.size()<genSimulated.size()){
			i1 = 0;
			for(int k=0;k<genSimulated.size();k++){
				if(this.insertion(i1)!=genSimulated.insertion(k)){
					if(genSimulated.insertion(k)==true){
						i1 = this.ins1.start();
					}else{
						i1 = this.ins1.end()+1;
					}
					genSimulated.setReadStarts(k, this.readStarts(i1));
				}else{
					genSimulated.setReadStarts(k, this.readStarts(i1));
				}
				i1++;
			}
		}
		return genSimulated;
	}
	
	/**
	 * Maps current genome to a simulated genome
	 * @param genSimulated
	 * @return Simulated genome
	 */
	public Genome mapTo1(Genome genSimulated){
	
		//i1 = counter for current genome
		//i2 = step
		//i3 = next coordinate
		//iSimulated = counter for simulated genome
		
		int i1;
		int i2;
		int i3;
		int iSimulated;
		
		if(this.size()>genSimulated.size()){
			iSimulated = 0;
			for(int i=0;i<this.size();i++){
				if(this.insertion(i)!=genSimulated.insertion(iSimulated)){
					continue;
				}else{
					genSimulated.setReadStarts(iSimulated, this.readStarts(i));
					iSimulated++;
				}
			}
		}else if(this.size()<genSimulated.size()){
			i1 = 0;
			i2 = 1;
			for(int k=0;k<genSimulated.size();k++){
				if(this.insertion(i1)!=genSimulated.insertion(k)){
					if(genSimulated.insertion(k)==true){
						
						//random point nearby
						do{
							i3 = this.ins1.start() + (int) (Math.random()*1000);
						}while(i3>this.size()-1);
						i2 = i1 - i3;
						i1 = i3;
						
						//***************************
						System.out.println("1," + i1);
						//***************************
						
						//incrementing step size (gives reduced variance)
						//i1 = this.ins1.start();
						//i2++;
						
						//random re-start points (gives reduced variance)
						//i1 = this.ins1.start() + (int) (Math.random()*this.ins1.length());
						//i2 = 1;
						
						//original code
						//if(Math.random()<0.5){
						//	i1 = this.ins1.start();
						//	i2 = 1;
						//}else{
						//	i1 = this.ins1.end();
						//	i2 = -1;
						//}
						
					}else{
						i1 = this.ins1.end()+1;
						
						//***************************
						System.out.println("2," + i1);
						//***************************
						
						i2 = 1;
					}
					genSimulated.setReadStarts(k, this.readStarts(i1));
				}else{
					genSimulated.setReadStarts(k, this.readStarts(i1));
				}
				i1+=i2;
				
				//**************************
				System.out.println("0," + i1);
				//**************************
				
				
				
			}
		}
		return genSimulated;
	}

	/**
	 * Maps current genome to a simulated genome
	 * @param genSimulated
	 * @return Simulated genome
	 */
	public Genome mapTo(Genome genSimulated){
	
		//i1 = counter for current genome
		//i3 = next coordinate
		//bSequencedUsed = flag for whether sequenced insertion is completely used
		
		int i1;
		int i3;
		boolean bSequencedUsed;
		
		//loading simulated non-insertion positions
		i1 = -1;
		for(int i=0;i<genSimulated.size();i++){
			if(!genSimulated.insertion(i)){
				i1++;
				genSimulated.setReadStarts(i, this.readStarts(i1));
			}else{
				i1=this.ins1.end();
			}
		}
		
		//loading simulated insertion positions
		i1 = this.ins1.start()-1;
		bSequencedUsed = false;
		for(int i=0;i<genSimulated.size();i++){
			if(genSimulated.insertion(i)){
				i1++;
				if(this.insertion(i1) && bSequencedUsed==false){
					genSimulated.setReadStarts(i, this.readStarts(i1));
				}else{
					bSequencedUsed = true;
					do{
						i3 = this.ins1.start()+(int) (Math.random()*1000);
					}while(i3>this.size()-1);
					genSimulated.setReadStarts(i, this.readStarts(i3));
				}
			}
		}
		return genSimulated;
	}

	private class BasePosition{
		
		/**Number of reads starting at position**/
		private int iReadStarts;
		
		/**Read depth**/
		private int iDepth;
		
		/**Indicator for whether an insertion**/
		private boolean bInsertion;
		
		private BasePosition(boolean bInsertion){
			this.bInsertion=bInsertion;
		}
	
		private void putReadStarts(int iReadStarts){
			this.iReadStarts=iReadStarts;
		}
		
		private int readStarts(){
			return iReadStarts;
		}
		
		private boolean insertion(){
			return bInsertion;
		}
		
		private void putDepth(int iDepth){
			this.iDepth = iDepth;
		}
		
		private int depth(){
			return iDepth;
		}
	}
}