package agents;

import repast.simphony.context.Context;
import java.util.ArrayList;
import java.util.Random;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;
import util.Const;
import util.SimulationClock;

@SuppressWarnings("rawtypes")
public class StationAgent {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private String name;
	private int sequence;
	private ArrayList<Integer> estimatedArrivals;
	private int passengersCount = 0;
	private int arrivalTime;
	private final int ENTRANCE_Y_POSITION;

	public int getSequence() {
		return sequence;
	}
	
	public String getName() {
		return name;
	}
	
	public StationAgent(
			String name,
			int sequence,
			ArrayList<Integer> estimatedArrivals,
			ContinuousSpace<Object> space,
			Grid<Object> grid,
			int arrivalTime,
			int entranceYPosition) {
		this.name = name;
		this.space = space;
		this.grid = grid;
		this.sequence = sequence;
		this.estimatedArrivals = estimatedArrivals;
		this.arrivalTime = arrivalTime;
		this.ENTRANCE_Y_POSITION = entranceYPosition;
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		int newPassangersCount = this.getNewPassengersCount();
		addPassangers(newPassangersCount);
	}
	
	private int getNewPassengersCount() {
		int hour = SimulationClock.getCurrentHour();
		int minutes = SimulationClock.getCurrentMinutes();
		
		int amountAtHour = this.estimatedArrivals.get(hour);
		double m = amountAtHour/60.0;
		double newPassengersCount = (m*minutes) - this.passengersCount;
		
		// add some random passangers
		double random = new Random().nextDouble();
		double randValue = Const.RANDOM_FACTOR_MIN + (random * (Const.RANDOM_FACTOR_MAX - Const.RANDOM_FACTOR_MIN));
		newPassengersCount = newPassengersCount*randValue;
		
		if (newPassengersCount < 0) newPassengersCount = 0;
		
		return (int)newPassengersCount;
	}
	
	@SuppressWarnings("unchecked")
	private void addPassangers(int amount) {
		for(int i = 0; i < amount; i++) {
			StationAgent goesTo = getRandomStation();
			PassengerAgent p = new PassengerAgent(this.space, this.grid, this, goesTo, this.ENTRANCE_Y_POSITION);
			this.passengersCount++;
			Context context = ContextUtils.getContext(this);
			context.add(p);
			NdPoint point = space.getLocation(this);
			int x = (int)point.getX();
			
			x += p.getDirection();
			
			space.moveTo(p, x, this.ENTRANCE_Y_POSITION);
			grid.moveTo(p, x, this.ENTRANCE_Y_POSITION);
		}
	}
	
	private StationAgent getRandomStation() {
		ArrayList<StationAgent> stations = new ArrayList<StationAgent>();

		Iterable<Object> objects = grid.getObjects();
		for (Object o : objects) {
			if (o.getClass() == StationAgent.class) {
				stations.add((StationAgent)o);
			}
		}

		Random rand = new Random();
		int randomIndex = rand.nextInt(stations.size()-1);
		
		StationAgent randomStation = stations.get(randomIndex);
		if (randomStation.getName() == this.getName()) return this.getRandomStation();
		return randomStation;
	}
	
	public int getArrivalTime() {
		return this.arrivalTime;
	}
	
	public void removePassenger() {
		this.passengersCount--;
	}
	
	public int getPassangersCount() {
		return this.passengersCount;
	}
	
}
