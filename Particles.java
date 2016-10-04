//Simulation for particle interaction
//Written by: Tom Bock
//Finished: 22/09/15
import java.io.IOException;
import java.util.ArrayList;

public class Particles {
	//The reason for the particles to be stored as an array of doubles in an array list is to
	//overcome the problem of not having a set amount particles in the simulation
	//Particles will be in form: (X, Y, Xvelocity, Yvelocity)
	ArrayList<double []> list;
	//These next 4 arrays represent the accelerations on the X and Y axis of the previous
	//timestep and the current timestep (both are needed for Verlet integration)
	private double [] accX;
	private double [] accY;
	private double [] accXTS;
	private double [] accYTS;
	//This array will be used to store the pattern of attractive and repulsive beads
	private int [] chainPattern;
	private double temperature = 1;
	private int chainLength;
	private double bigParticleInteraction = 0;
	private double bigParticleInteractionVal = 0;
	private double tsCounter = 0;
	private int w;
	private WriteFile data = new WriteFile("./SaveFileData1234.txt", true);     

	public Particles ( int n,int l,int w){
		list = new ArrayList<double []>();
		chainLength = l;
		this.w = w;
		createNewParticles(n,l,w);
	}
	public int [] getChainPattern(){
		return this.chainPattern;
	}
	public void setChainPattern(int [] chainPattern){
		this.chainPattern = chainPattern;
	}

	//This method is going to add "n" particles to the list with 0 base velocity
	public void createNewParticles(int n, int chainLength, int width){
		int rowLength = (Math.round(width / ((2*chainLength)+1)))*chainLength;
		double x = 1;
		double y = 1;
		int counter = 0;
		for (int i = 0; (n)>i;i++){
			x+=1.1225;

			if (counter == rowLength){
				y+=1.1225;
				x = 2.1225;
				counter = 0;
			}
			double [] d = {x,y,0,0};
			(this.list).add(d);
			counter++;
		}
		int s = (this.list).size();
		this.accX = new double[s];
		this.accY = new double[s];
		this.accXTS = new double[s];
		this.accYTS = new double[s];
	}

	//This method will remove "n" particles from the simulation
	public void removeParticles(int n){
		for (int i = 0; n>i;i++){
			int s = (this.list).size();
						if (s != 0){
			(this.list).remove((s-1));
			}
		}
	}
	//Getter and Setter for the list
	public ArrayList<double []> getList(){
		return this.list;
	}
	public void setList (ArrayList<double []> list){
		int s = list.size();
		this.list = list;
		this.accX = new double[s];
		this.accY = new double[s];
		this.accXTS = new double[s];
		this.accYTS = new double[s];
	}

	//Generic toString
	public String toString(){
		String output = "";
		for (double d [] : (this.list)){
			output+=("X: "+d[0]+" Y: "+d[1]+" X-Vel: "+d[2]+" Y-Vel: "+d[3]+"\n");
		}
		return output;
	}
	//Integration algorithm
	public void integrate(int width, int height, double pX, double pY, double pR, double pX2, double pY2, double pR2){
		int s = (this.list).size();
		//System.out.println(s);
		double x;
		double y;
		double velX;
		double velY;
		//the time step
		double ts = (1.0/500);
		tsCounter += ts;
		for (int i = 0; i<s; i++){
			//Euler Integration
			//velX = ((this.list).get(i))[2] + ((this.accX)[i]*ts);
			//velY = ((this.list).get(i))[3] + ((this.accY)[i]*ts);
			//x = ((this.list).get(i))[0] + (((this.list).get(i))[2]*ts);
			//y = ((this.list).get(i))[1] + (((this.list).get(i))[3]*ts);
			//Verlet Integration
			x = ((this.list).get(i))[0]+(((this.list).get(i))[2]*ts)+((0.5*(this.accXTS)[i])*(Math.pow(ts, 2)));
			y = ((this.list).get(i))[1]+(((this.list).get(i))[3]*ts)+((0.5*(this.accYTS)[i])*(Math.pow(ts, 2)));

			//Repeating boundary condition imposed here
			if (x > width){
				x=(x-width);
			}
			else if (x < 0.0){
				x=(width+x);
			}
			if (y > height){
				y=(y-height);
			}
			else if (y < 0.0){
				y=(height+y);
			}
			((this.list).get(i))[0] = x;
			((this.list).get(i))[1] = y;
		}
		forceCalculation(width, height, pX, pY, pR, pX2, pY2, pR2);
		for (int i = 0; i<s; i++){
			velX = ((this.list).get(i))[2] + (((this.accX)[i]+(this.accXTS)[i])*0.5)*ts;
			velY = ((this.list).get(i))[3] + (((this.accY)[i]+(this.accYTS)[i])*0.5)*ts;
			((this.list).get(i))[2] = velX;
			((this.list).get(i))[3] = velY;
		}
	}

	public void forceCalculation(double width, double height, double bigPX, double bigPY, double radiusP, double bigPX2, double bigPY2, double radiusP2){
		int s = (this.list).size();
		double rcut_repulsiveBig=Math.pow(2.0, (1.0/3));
		double rcut_repulsive=Math.pow(2.0, (1.0/6));
		double distanceX;
		double distanceY;
		double forcesX = 0;
		double forcesY = 0;
		double force = 0;
		for (int i = 0; i<s;i++){
			this.accX[i] = this.accXTS[i];
			this.accY[i] = this.accYTS[i];
			this.accXTS[i] = 0;
			this.accYTS[i] = 0;
		}
		//loops for all items in the array list (so all particles)
		for (int i = 0; i<s;i++){
			//variable setup is needed for every iteration (mostly for clarity)
			double lastX = ((this.list).get(i))[0];
			double lastY = ((this.list).get(i))[1];
			for (int j = i+1; j<s;j++){
				double tempX = ((this.list).get(j))[0];
				double tempY = ((this.list).get(j))[1];
				force = 0;
				forcesX = 0;
				forcesY = 0;
				distanceX = lastX-tempX;
				distanceY = lastY-tempY;
				distanceX -= width *Math.rint(distanceX/width);
				distanceY -= height*Math.rint(distanceY/height);
				double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));

				//----- Bond forces -----
				if (i+1==j) {
					if (j % (this.chainLength) != 0) {
						force = 1200*(1.1225-distance);
					}
				}

				//----- bead/bead interactions -----
				if (!((j % (this.chainLength) != 0)&&(i+1==j))) {
					if (distance <= 2.5){
						//Force for van der Waals
						if ( this.chainPattern[i % (this.chainLength)] * this.chainPattern[j % (this.chainLength)] == 1) {
							force += 48*(Math.pow(distance, -13)) - 24*(Math.pow(distance, -7));
						}
						else {
							if (distance <= rcut_repulsive) {
								force += 12*(Math.pow(distance, -13)) - 6*(Math.pow(distance, -7));
							}
						}
					}
				}		
				forcesX = force*distanceX/distance;
				forcesY = force*distanceY/distance;
				this.accXTS[i] += forcesX;
				this.accXTS[j] -= forcesX;					
				this.accYTS[i] += forcesY;
				this.accYTS[j] -= forcesY;					
			}


			//----- forces from big particles -----      
			//----- big particle 1 -----
			force = 0;
			distanceX = lastX-bigPX;
			distanceY = lastY-bigPY;
			distanceX -= width*Math.rint(distanceX/width );
			distanceY -= height*Math.rint(distanceY/height);
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2)) - radiusP;
			//----- bead/bead interactions -----
			if (distance <= 5){
				double tempPat = 0;
				if (this.bigParticleInteractionVal < 0){ //red
					if(this.chainPattern[i % (this.chainLength)] == 0){
						tempPat = 1;
					}
				}
				else { //blue
					tempPat = this.chainPattern[i % (this.chainLength)];
				}
				//Force for van der Waals
				if ( tempPat == 1) {
					force = 4*bigParticleInteraction*(6*(Math.pow(distance, -7)) - 3*(Math.pow(distance, -4)));
					/*         try{     
         data.writeToFile(tsCounter+" 1 A "+i+" "+distance+" "+force+"\r\n");
          }
   	   catch (IOException e){
		   System.out.println("Please don't do this. :'(");
	   }*/
				}
				else {
					if (distance <= rcut_repulsiveBig) {
						force = 4*bigParticleInteraction*(6*(Math.pow(distance, -7)) - 3*(Math.pow(distance, -4)));
						/*            try{     
                data.writeToFile(tsCounter+" 1 B "+i+" "+distance+" "+force+"\r\n");
                }
         	   catch (IOException e){
      		   System.out.println("Please don't do this. :'(");
      	   }*/
					}
				}
				distance += radiusP;
				forcesX = force*distanceX/distance;
				forcesY = force*distanceY/distance;
				this.accXTS[i] += forcesX;
				this.accYTS[i] += forcesY;
			}

			//----- big particle 2 -----
			force = 0;
			distanceX = lastX-bigPX2;
			distanceY = lastY-bigPY2;
			distanceX -= width *Math.rint(distanceX/width );
			distanceY -= height*Math.rint(distanceY/height);
			distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2)) - radiusP2;
			//----- bead/bead interactions -----
			if (distance <= 5){
				double tempPat = 0;
				if (this.bigParticleInteractionVal < 0){ //red
					if(this.chainPattern[i % (this.chainLength)] == 0){
						tempPat = 1;
						//System.out.println(i+" red " +tempPat);
					}
				}
				else { //blue
					tempPat = this.chainPattern[i % (this.chainLength)];
					//System.out.println(i+" blue "+tempPat);
				}
				//Force for van der Waals
				if ( tempPat == 1) {
					force = 4*bigParticleInteraction*(6*(Math.pow(distance, -7)) - 3*(Math.pow(distance, -4)));
					/*           try{      
               data.writeToFile(tsCounter+" 2 A "+i+" "+distance+" "+force+"\r\n");
               }
        	   catch (IOException e){
     		   System.out.println("Please don't do this. :'(");
     	   } */
				}
				else {
					if (distance <= rcut_repulsiveBig) {
						force = 4*bigParticleInteraction*(6*(Math.pow(distance, -7)) - 3*(Math.pow(distance, -4)));
						/*             try{     
                 data.writeToFile(tsCounter+" 2 B "+i+" "+distance+" "+force+"\r\n");
                 }
          	   catch (IOException e){
       		   System.out.println("Please don't do this. :'(");
       	   }*/
					}
				}
				distance += radiusP2;
				forcesX = force*distanceX/distance;
				forcesY = force*distanceY/distance;
				this.accXTS[i] += forcesX;
				this.accYTS[i] += forcesY;
			}
		}

	}




	//Returns the total Kinetic energy for the particles in the system
	public double calcKineticEnergy(){
		double energy = 0;
		double vel;
		int s = (this.list).size();
		for (int i = 0; i<s;i++){
			vel = Math.sqrt((Math.pow(((this.list).get(i)[2]), 2)) + (Math.pow(((this.list).get(i)[3]), 2)));
			energy += (0.5*(Math.pow(vel, 2)));
		}
		return energy;
	}
	//Returns the temperature based on thermal energy from kinetic energy
	public double calcTemp(){
		double kinetic = calcKineticEnergy();
		double temp = kinetic * (1.0/((this.list).size()));
		return temp;
	}
	//These two methods belong together given that at any point where the temperature is changed the velocities
	//will also need to be changed. 
	public void setTemperature(double temperature){
		scaleVelWithTemp(temperature);
		this.temperature = temperature;
	}
	public void setChainLength(int chainLength){
		this.chainLength = chainLength;
	}
	public void setBigParticleInteraction(double bigParticleInteraction, double bigParticleInteractionVal){
		this.bigParticleInteraction = bigParticleInteraction;
		this.bigParticleInteractionVal = bigParticleInteractionVal;
	}
	public void scaleVelWithTemp( double nextTemp){
		double current = calcTemp();
		if (current != 0){
			double t = Math.pow((nextTemp/current), 0.5);
			int s = (this.list).size();
			for (int i = 0; i<s;i++){
				((this.list).get(i))[2] = t*((this.list).get(i))[2];
				((this.list).get(i))[3] = t*((this.list).get(i))[3];
			}
		}
	}
	//Returns the potential energy contained in the system calculated through the potential between two particles
	//NOTICE THAT THE DISTANCE ALGORITHM HERE IS COPIED AND PASTED FROM THE FORCE CALCULATION (GIVEN THAT THESE TWO METHODS
	//DO PRACITCALLY THE SAME THING) SO IT WOULD BE MUCH MORE EFFECIENT TO PUT DISTANCE CALCULATION AS ITS OWN METHOD LATER
	public double calcPotentialEnergy(int width, int height){
		double energy = 0;
		double distanceX;
		double distanceY;
		int s = (this.list).size();
		for (int i = 0; i<s;i++){
			double lastX = ((this.list).get(i))[0];
			double lastY = ((this.list).get(i))[1];
			for (int j = i+1; j<s;j++){
				double tempX = ((this.list).get((j)))[0];
				double tempY = ((this.list).get((j)))[1];
				distanceX = Math.sqrt(Math.pow((lastX-tempX), 2));
				distanceY = Math.sqrt(Math.pow((lastY-tempY), 2));
				if (distanceX > (width/2)){
					distanceX = width-distanceX;
				}
				if (distanceY > (height/2)){
					distanceY = height - distanceY;
				}
				double distance = Math.sqrt((Math.pow(distanceX, 2)+Math.pow(distanceY, 2)));
				if (distance != 0){
					energy += (Math.pow(distance, -3)) - (Math.pow(distance, -2.5));
				}
			}
		}
		return energy;
	}
}
