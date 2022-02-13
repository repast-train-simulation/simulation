package agents;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import util.SimulationClock;

public class ClockAgent {
	
	public double tickCount() {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		double currentTime = schedule.getTickCount();
		return currentTime;
	}
	
	public String getLabel() {
		return SimulationClock.getCurrentHour()+":"+SimulationClock.getCurrentMinutes();
	}
}
