package agents;

import java.util.ArrayList;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import util.Const;
import util.SimulationClock;

/*
 * En la actualidad, el tren corre a una velocidad promedio de 65 km/h. Hay 14 coches de clase estándar que pueden acomodar hasta 72 pasajeros cada uno en configuración 3 + 2.
 * El recorrido total de la línea Roca desde Constitución a La Plata dura 70minutos
 * 	Suponiendo que los trenes que llegan a las estaciones terminales, vuelven a salir inmediatamente, y utilizando el cronograma de horarios,
 *  podemos estimar que la línea cuenta con 10 trenes 
 */

// TODO: Passangers must only get ride on train when the direction is the correct one

//@SuppressWarnings("rawtypes")
public class TrainAgent {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private final int MAX_CAPACITY = RunEnvironment.getInstance().getParameters().getInteger("train_capacity");
	private int STOP_TIME = RunEnvironment.getInstance().getParameters().getInteger("train_stop_time_minutes");
	private boolean onPlatform;
	private int ticksSpentOnPlatform = 0;
	private ArrayList<PassengerAgent> passengersOn;
	private ArrayList<Integer> scheduledStops;
	private int lastStop;
	private int firstStop;
	private int trainDirection = -1; //1:right, -1:left
	private int tickCount = 0;
	private double velocity = 0;
	private int startsAtMinute = 0;
	
	public TrainAgent(ContinuousSpace<Object> space, Grid<Object> grid, ArrayList<Integer> scheduledStops, double velocity, int startsAtMinute) {
		this.space = space;
		this.grid = grid;
		this.scheduledStops = scheduledStops;
		this.lastStop = scheduledStops.get(scheduledStops.size()-1);
		this.firstStop = scheduledStops.get(0);
		this.velocity = velocity;
		this.passengersOn = new ArrayList<PassengerAgent>();
		this.startsAtMinute = startsAtMinute;
		
		System.out.println(3);
	}
	
	public boolean isIddle() {
		return this.startsAtMinute > SimulationClock.getElapsedTimeInMinutes();
	}
	
	public int getDirection() {
		return this.trainDirection;
	}

	public boolean isOnPlatform() {
		return onPlatform;
	}

	public boolean maxCapacityReached() {
		return this.passengersOn.size() >= this.MAX_CAPACITY;
	}

	public String getLabel() {
		String label = String.valueOf(this.passengersOn.size())+"/"+String.valueOf(this.MAX_CAPACITY);
		if (this.trainDirection == 1) label = label + " ->";
		else label = "<-" + label;
		return label;
	}

	public void addPassenger(PassengerAgent passenger) {
		this.passengersOn.add(passenger);
	}
	
	public void removePassenger(PassengerAgent passenger) {
		int index = -1;
		index = this.passengersOn.indexOf(passenger);
		if (index > 0) {
			this.passengersOn.remove(index);
		}
	}

	@ScheduledMethod(start = 1, interval = 1, priority = 3)
	public void step() {
		if (!this.isWaitingOnPlatform() && this.startsAtMinute < SimulationClock.getElapsedTimeInMinutes()) {
			GridPoint nextPoint = getNextLocation();
			moveTowards(nextPoint);	
		}
		else {
			if (this.haveToChangeDirection()) changeDirection();
			dropOffPassengers();
		}
	}
	
	private boolean haveToChangeDirection() {
		return (this.isTheFirstStop() && this.trainDirection == -1) || (this.isTheLastStop() && this.trainDirection == 1);
	}
	
	@ScheduledMethod(start = 0, interval = 1, priority = 1)
	public void incrementTickCount() {
		this.tickCount++;
	}
	
	private void dropOffPassengers() {
		StationAgent currentStation = getCurrentStation();
		ArrayList<PassengerAgent> passengersToDropOff = 
				new ArrayList<PassengerAgent>();
		
		for (PassengerAgent passenger : this.passengersOn) {
			if (passenger.getDestinationStation().equals(currentStation))
				passengersToDropOff.add(passenger);
		}
		
		for (PassengerAgent passenger : passengersToDropOff)
			this.removePassenger(passenger);
	}
	
	private StationAgent getCurrentStation() {
		GridPoint myLocation = grid.getLocation(this);
		Object object = grid.getObjectAt(myLocation.getX(), (int)Const.STATIONS_Y_POSITION);
		if (object != null && object.getClass() == StationAgent.class) {
			return (StationAgent)object;
		}
		return null;
	}

	private void changeDirection() {
		this.trainDirection = this.trainDirection * (-1);
		this.ticksSpentOnPlatform = 0;
	}
	
	private GridPoint getNextLocation() {
		GridPoint currentLocation = this.grid.getLocation(this);
		double x = currentLocation.getX();
		x = x + (this.velocity*this.trainDirection*(double)this.tickCount);
		return new GridPoint((int)x, currentLocation.getY());
	}

	private boolean isWaitingOnPlatform() {
		int timeSpentOnPlatform = (int) (this.ticksSpentOnPlatform*Const.MINUTES_PER_TICK);
		if (this.hasAStopOnCurrentLocation() && timeSpentOnPlatform < this.STOP_TIME) {
			this.ticksSpentOnPlatform++;
			this.onPlatform = true;
		}
		else {
			this.ticksSpentOnPlatform = 0;
			this.onPlatform = false;
		}
		
		return this.onPlatform;
	}

	private boolean hasAStopOnCurrentLocation() {
		GridPoint currentLocation = this.grid.getLocation(this);
		for (int i : this.scheduledStops) {
			if (i == currentLocation.getX()) return true;
		}
		
		return false;
	}

	private boolean isTheLastStop() {
		GridPoint currentLocation = this.grid.getLocation(this);
		return (int)currentLocation.getX() == this.lastStop;
	}

	private boolean isTheFirstStop() {
		GridPoint currentLocation = this.grid.getLocation(this);
		return (int)currentLocation.getX() == this.firstStop;
	}

	private void moveTowards(GridPoint nextPoint) {
		if (!nextPoint.equals(grid.getLocation(this))){
			NdPoint currentPoint = space.getLocation(this);
			NdPoint nextNdPoint = new NdPoint(nextPoint.getX(), nextPoint.getY());
			
			double angle = SpatialMath.calcAngleFor2DMovement(space, currentPoint, nextNdPoint);
			
			space.moveByVector(this, 1, angle, 0);
			
			currentPoint = space.getLocation(this);
			grid.moveTo(this, (int)currentPoint.getX(), (int)currentPoint.getY());
		}
	}
}