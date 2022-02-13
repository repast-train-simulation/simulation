package models;

import java.util.ArrayList;

public class StationModel {
	private int position;
	private String name;
	private ArrayList<Integer> arrivals;
	private int arrivalTime;
	
	public StationModel(String name, int position, ArrayList<Integer> arrivals, int arrivalTime) {
		this.name = name;
		this.position = position;
		this.arrivals = arrivals;
		this.arrivalTime = arrivalTime;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getPosition() {
		return this.position;
	}
	
	public ArrayList<Integer> getArrivals(){
		return this.arrivals;
	}
	
	public int getArrivalTime() {
		return this.arrivalTime;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
}
