package util;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;

public class SimulationClock {
	public static int getCurrentMinutes() {
		double currentMinutes = tickCount() * Const.MINUTES_PER_TICK;
		if ((int)currentMinutes == 60) return 0;
		
		return (int)currentMinutes;
	}
	
	public static int getCurrentHour() {
		double currentHour = (tickCount() * Const.MINUTES_PER_TICK) / 60;
		if ((int)currentHour == 24) return 0;
		
		return (int)currentHour;
	}
	
	public static int getElapsedTimeInMinutes() {
		return (int)(tickCount() * Const.MINUTES_PER_TICK);
	}

	
	private static double tickCount() {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		double currentTime = schedule.getTickCount();
		return currentTime;
	}
}
