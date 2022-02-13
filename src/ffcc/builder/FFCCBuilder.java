package ffcc.builder;

import java.io.IOException;
import java.util.ArrayList;

import util.*;
import agents.ClockAgent;
import agents.RailwayAgent;
import agents.StationAgent;
import models.StationModel;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;

public class FFCCBuilder implements ContextBuilder<Object> {	
	private ArrayList<StationModel> stations;
	private double totalLineLengthMeters = 0;
	
	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId(Const.PROJECT_NAME);
		
		ContinuousSpace<Object> space = createSpace(context);
		Grid<Object> grid = createGrid(context);
		
		addClockToSpace(context, space, grid);
		
		try {
			this.stations = getStationsFromFile();
			ArrayList<Integer> stationsPositions = addStationsToSpace(this.stations, context, space, grid);
			int trainSpawnPosition = stationsPositions.get(0);
			context.add(new RailwayAgent(space, grid, stationsPositions, this.totalLineLengthMeters, trainSpawnPosition));
		} catch (IOException e) {
			System.out.println("Cannot add agents to current space:");
			e.printStackTrace();
		}
		
		return context;
	}
	
	private void addClockToSpace(Context<Object> context, ContinuousSpace<Object> space, Grid<Object> grid) {
		ClockAgent clock = new ClockAgent();
		context.add(clock);
		
		grid.moveTo(clock, (int)(Const.SPACE_WIDTH*0.98), (int)(Const.SPACE_HEIGHT*0.95));
		space.moveTo(clock, Const.SPACE_WIDTH*0.98, Const.SPACE_HEIGHT*0.95);
	}
	
	private ArrayList<Integer> addStationsToSpace(ArrayList<StationModel> stations, Context<Object> context, ContinuousSpace<Object> space, Grid<Object> grid){
		ArrayList<Integer> stationsPositions = new ArrayList<Integer>();
		int i = 0;
		
		for (StationModel s : stations) {
			StationAgent station = new StationAgent(s.getName(), i, s.getArrivals(), space, grid, s.getArrivalTime(), Const.SPACE_HEIGHT);
			
			context.add(station);
			
			grid.moveTo(station, s.getPosition(), (int)Const.STATIONS_Y_POSITION);
			space.moveTo(station, s.getPosition(), Const.STATIONS_Y_POSITION);
			
			stationsPositions.add(s.getPosition());
			i++;
		}
		
		return stationsPositions;
	}
	
	private ArrayList<StationModel> getStationsFromFile() throws IOException {
		ArrayList<StationModel> stations = new ArrayList<StationModel>();
		int totalLineLength = 0;
		
		// TODO: get file location by params
		ArrayList<String> file_rows = 
				util.Utils.ReadLinesFromFile("/home/feder/Documents/ffcc_data.csv");
		
		for (int i = 1; i < file_rows.size(); i++) {
			ArrayList<String> data = new ArrayList<String>(); 
			for (String a : file_rows.get(i).split(";")) {
				data.add(a);
			}
			
			String name = data.get(0);
			
			ArrayList<Integer> arrivals = new ArrayList<Integer>();
			for(String a : data.subList(1, 25)) { 
				arrivals.add(Integer.parseInt(a));
			}
			int arrivalTime = Integer.parseInt(data.get(25)); 
			if (arrivalTime > totalLineLength) totalLineLength = arrivalTime;
			
			StationModel station = new StationModel(name, arrivalTime, arrivals, arrivalTime);
			stations.add(station);
		}
		
		reCalculateLocationByDistance(stations);
		
		return stations;
	}
	
	private void reCalculateLocationByDistance(ArrayList<StationModel> stations) {
		double totalDistance = stations.get(stations.size()-1).getPosition();
		double borderDistance = totalDistance*0.01;
		double border = (borderDistance/totalDistance)*Const.SPACE_WIDTH;
		totalDistance = totalDistance + (borderDistance*2);
		
		int i = 0;
		for (StationModel station : stations) {
			double position = station.getPosition();
			this.totalLineLengthMeters = position;
			
			double lineProgression = position/totalDistance;
			
			position = lineProgression*Const.SPACE_WIDTH;
			if (i==0) position+=border;
			station.setPosition((int)position);
			i++;
		}
	}
	
	private ContinuousSpace<Object> createSpace(Context<Object> context) {
		ContinuousSpaceFactory spacesFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spacesFactory.createContinuousSpace(
				"space", 
				context, 
				new RandomCartesianAdder<Object>(), 
				new repast.simphony.space.continuous.WrapAroundBorders(), 
				Const.SPACE_WIDTH, Const.SPACE_HEIGHT);
		
		return space;
	}
	
	private Grid<Object> createGrid(Context<Object> context) {
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);		
		Grid<Object> grid = gridFactory.createGrid(
				"grid", 
				context ,
				new  GridBuilderParameters <Object>(
						new  repast.simphony.space.grid.WrapAroundBorders(),
						new  SimpleGridAdder <Object>(),
						true, 
						Const.SPACE_WIDTH, Const.SPACE_HEIGHT));
		return grid;
	}

}
