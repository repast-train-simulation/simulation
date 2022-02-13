package agents;

import java.util.ArrayList;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;
import util.Const;

@SuppressWarnings("rawtypes")
public class RailwayAgent {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int trainTrackHeigth = 45;
	private int trainSpawnPosition;
	private double totalLineLengthMeters;
	private ArrayList<Integer> stationsPositions;
	private double trainVelocity;

	public RailwayAgent(
			ContinuousSpace<Object> space, 
			Grid<Object> grid, 
			ArrayList<Integer> stationsPositions, 
			double totalLineLengthMeters,
			int trainSpawnPosition) {
		this.space = space;
		this.grid = grid;
		this.stationsPositions = stationsPositions;
		this.totalLineLengthMeters = totalLineLengthMeters;
		this.trainVelocity = calculateTrainVelocity();
		this.trainSpawnPosition = trainSpawnPosition;
	}
	
	private double calculateTrainVelocity() {
		int VELOCITY = RunEnvironment.getInstance().getParameters().getInteger("train_velocity_kmh");
		int STOP_TIME = RunEnvironment.getInstance().getParameters().getInteger("train_stop_time_minutes");
		
		// convert km/h to spacesUnits/ticks
		double spaceUnitsPerKilometer = this.totalLineLengthMeters/((double)Const.SPACE_WIDTH);
		double ticksPerHour = (Const.MINUTES_PER_TICK)/60;
		
		double trainVelocity_SpaceUnitsPerTick = VELOCITY*spaceUnitsPerKilometer/ticksPerHour;
		
		return trainVelocity_SpaceUnitsPerTick;
	}
	
	@ScheduledMethod(start = 1, interval = 0, priority = 1)
	public void init() {
		addTrains();
	}	
	
	@SuppressWarnings("unchecked")
	private void addTrains() {		
		int quantity = RunEnvironment.getInstance().getParameters().getInteger("number_of_trains");
		int trainInterval = RunEnvironment.getInstance().getParameters().getInteger("train_interval");
		
		int trainIntervalCount = 0;
		
		for(int i = 0; i < quantity; i++) {
			TrainAgent train = new TrainAgent(this.space, this.grid, stationsPositions, this.trainVelocity, trainIntervalCount);

			Context context = ContextUtils.getContext(this);

			context.add(train);

			space.moveTo(train, this.trainSpawnPosition, this.trainTrackHeigth);
			grid.moveTo(train, this.trainSpawnPosition, this.trainTrackHeigth);
			trainIntervalCount += trainInterval;
		}
	}
	
}
