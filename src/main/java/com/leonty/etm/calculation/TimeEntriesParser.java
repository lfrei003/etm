package com.leonty.etm.calculation;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.leonty.etm.time.WorkDay;
import com.leonty.etm.time.WorkEntry;
import com.leonty.etm.time.WorkWeek;
import com.leonty.etm.time.WorkWeeks;

public class TimeEntriesParser {

	public static WorkWeeks getWorkWeeks(DateTime startDate, DateTime endDate, List<TimeEntry> timeEntries) {
		
		DateTime start = new DateTime(startDate);
		DateTime end = new DateTime(endDate);
		
		timeEntries = fixLastEntry(timeEntries, endDate);
		
		List<WorkWeek> weeks = new ArrayList<WorkWeek>();	
		
		DateTime currentTimeDayStart = new DateTime(start);
		int dayCount = 0;
		
		while (currentTimeDayStart.isBefore(end)) {
			if (dayCount % 7 == 0) {
				weeks.add(new WorkWeek());
			}
			
			DateTime currentTimeDayEnd = currentTimeDayStart.plusHours(24);
			
			WorkDay workDay = new WorkDay(currentTimeDayStart.toDate(), currentTimeDayEnd.toDate());
			
			workDay.addEntries(getWorkEntriesForDuration(currentTimeDayStart, currentTimeDayEnd, timeEntries));
	
			weeks.get(weeks.size() - 1).addDay(workDay);			
			
			currentTimeDayStart = currentTimeDayStart.plusHours(24);
			
			dayCount++;						
		}
		return new WorkWeeks(weeks);
	}
	
	// signOut is null so employee is still working
	private static List<TimeEntry> fixLastEntry(List<TimeEntry> timeEntries, DateTime endDate) {
		
		if (timeEntries.size() > 0 
				&& timeEntries.get(timeEntries.size() - 1).getTimeOut() == null) {
			timeEntries.get(timeEntries.size() - 1).setTimeOut(endDate);
		}
		
		return timeEntries;
	}
	
	private static List<WorkEntry> getWorkEntriesForDuration(DateTime start, DateTime end, List<TimeEntry> timeEntries) {
		
		List<WorkEntry> workEntries = new ArrayList<WorkEntry>();
		
		for (TimeEntry timeEntry : timeEntries) {
			
			if (isInsideTimeSpan(timeEntry, start, end)) {
				workEntries.add(new WorkEntry(
					getTimeIn(timeEntry, start).toDate(), 
					getTimeOut(timeEntry, end).toDate(), 
					timeEntry.getWage(),
					timeEntry.getJobTitle()));				
			}
		}
		
		return workEntries;
	} 

	// if work started before the start of the day - cut the limit off
	private static DateTime getTimeIn(TimeEntry timeEntry, DateTime dayStart) {
		
		if (timeEntry.getTimeIn().isBefore(dayStart)) 
			return dayStart;

		return timeEntry.getTimeIn();
	}
	
	// if work is ending after the days end - cut it at the top
	private static DateTime getTimeOut(TimeEntry timeEntry, DateTime dayEnd) {
		
		if (timeEntry.getTimeOut().isAfter(dayEnd))
			return dayEnd;
			
		return timeEntry.getTimeOut();
	}
	
	/**
	 * Checks if time entry belongs to given timespan (usually a day)
	 * 
	 * @param time - Time object containing time in and time out
	 * @param start - start of the timespan (inclusive)
	 * @param end - end of the timespan
	 * @return
	 */
	private static boolean isInsideTimeSpan(TimeEntry time, DateTime start, DateTime end) {
		
		/* Time entry can be considered belonging to today only if it started yesterday and overlaps till today
		 * Or it started during today
		 * */
		
		// work started yesterday and still continues today or even later
		if (time.getTimeIn().isBefore(start) && time.getTimeOut().isAfter(start)) {
			return true;
		}
		
		// work started inside the time span. Start is inclusive
		if ((time.getTimeIn().isAfter(start) || time.getTimeIn().compareTo(start) == 0) 
				&& time.getTimeIn().isBefore(end)) {
			return true;
		}
		
		return false;
	}	
}
