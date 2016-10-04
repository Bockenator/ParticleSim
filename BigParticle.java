//Simulation for particle interaction
//Written by: Tom Bock
//Finished: 22/09/15
public class BigParticle {
	private double centreX;
	private double centreY;
	private double displacementX;
	private double displacementY;
	private double radius;

	public BigParticle(double x, double y, double r) {
		this.centreX = x;
		this.centreY = y;
		this.radius = r;
	}
	public double getDisplacementX() {
		return displacementX;
	}

	public void setDisplacementX(double displacementX) {
		this.displacementX = displacementX;
	}

	public double getDisplacementY() {
		return displacementY;
	}

	public void setDisplacementY(double displacementY) {
		this.displacementY = displacementY;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getCentreX() {
		return centreX;
	}

	public void setCentreX(double centreX) {
		this.centreX = centreX;
	}

	public double getCentreY() {
		return centreY;
	}

	public void setCentreY(double centreY) {
		this.centreY = centreY;
	}

}
