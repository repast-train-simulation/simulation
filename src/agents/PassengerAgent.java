package agents;

import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
//import exceptions.MovingAgentException;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
//import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
//import repast.simphony.space.continuous.NdPoint;

@SuppressWarnings("rawtypes")
public class PassengerAgent {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private StationAgent goesFrom;
	private StationAgent goesTo;
	private int direction; //1 -> rigth, -1 left;

	public PassengerAgent(ContinuousSpace<Object> space, Grid<Object> grid, StationAgent station, StationAgent destinationStation, int spawnYPosition) {
		this.space = space;
		this.grid = grid;
		this.goesFrom = station;
		this.goesTo = destinationStation;
		this.direction = getMyDirection();
		System.out.print("new passanger from ");
		System.out.print(station.getName());
		System.out.print(" to ");
		System.out.println(destinationStation.getName());
	}
	
	private int getMyDirection() {
		if (goesFrom.getSequence() < goesTo.getSequence()) return 1;
		else return -1;
	}
	
	public int getDirection() {
		return this.direction;
	}
	
	public StationAgent getDestinationStation() {
		return this.goesTo;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		moveTowards();
	}
	
	private TrainAgent trainAtStation() {
		GridPoint myPosition = grid.getLocation(this);
		
		Iterable<Object> objects = grid.getObjects();
		
		TrainAgent train = null;
		
		for (Object o : objects) {			 
			if (o.getClass() == TrainAgent.class) {
				train = (TrainAgent)o;
				
				GridPoint trainPosition = grid.getLocation(train);
				if (trainPosition.getX() == (myPosition.getX() - this.getDirection())) {
					return train;
				}
			}
		}
		
		return null;
	}
	
	private void moveTowards() {
		TrainAgent train = trainAtStation();
		GridPoint myLocation = grid.getLocation(this);
		int newYLocation = myLocation.getY()+1;
		
		int x = myLocation.getX() - this.getDirection();
		Object objectInFrontOfMe = grid.getObjectAt(x, newYLocation);
		if (objectInFrontOfMe == null) objectInFrontOfMe = grid.getObjectAt(myLocation.getX(), newYLocation);
		
		if (objectInFrontOfMe != null) {
			if (objectInFrontOfMe.getClass() == PassengerAgent.class) {
				// wait
			}
			else if (objectInFrontOfMe.getClass() == StationAgent.class && train != null) {
				getOnTheTrain(train);
			}
		}
		else {
			grid.moveTo(this, (int)myLocation.getX(), newYLocation);
			space.moveTo(this, (int)myLocation.getX(), newYLocation);
		}
	}
	
	private void getOnTheTrain(TrainAgent train) {
		if (!train.maxCapacityReached() && !train.isIddle() && this.direction == train.getDirection()) {
			Context context = ContextUtils.getContext(this);
			train.addPassenger(this);
			if (context.size() > 1) {
				this.goesFrom.removePassenger();
				context.remove(this);
			}
		}
	}
	
}
