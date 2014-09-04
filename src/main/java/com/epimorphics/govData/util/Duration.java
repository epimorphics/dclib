/******************************************************************
 * File:        Duration.java
 * Created by:  Stuart Williams
 * Created on:  13 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * $UkId:  $
 *****************************************************************/

package com.epimorphics.govData.util;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Duration {

	private int years  = 0;
	private int months = 0;
	private int days   = 0;
	private int hours  = 0;
	private int mins   = 0;
	private int secs   = 0;
	
	protected static int DURATION_YEARS = 2;
	protected static int DURATION_MONTHS = 4;
	protected static int DURATION_DAYS = 6;
	protected static int DURATION_HOURS = 9;
	protected static int DURATION_MINUTES = 11;
	protected static int DURATION_SECONDS = 13;
	
	protected static final String DURATION_REGEX = "P(([0-9]+)Y)?(([0-9]+)M)?(([0-9]+)D)?(T(([0-9]+)H)?(([0-9]+)M)?(([0-9]+)S)?)?";
	//                                                ^2          ^4          ^6         ^7 ^9          ^11         ^13

	public Duration(String duration) {
		Pattern p = Pattern.compile(DURATION_REGEX);
		Matcher m = p.matcher(duration);

		if (m.matches()) {
			String s = m.group(DURATION_YEARS);
			if (s != null && !s.equals(""))
				years = Integer.parseInt(s);

			s = m.group(DURATION_MONTHS);
			if (s != null && !s.equals(""))
				months = Integer.parseInt(s);

			s = m.group(DURATION_DAYS);
			if (s != null && !s.equals(""))
				days = Integer.parseInt(s);

			s = m.group(DURATION_HOURS);
			if (s != null && !s.equals(""))
				hours = Integer.parseInt(s);

			s = m.group(DURATION_MINUTES);
			if (s != null && !s.equals(""))
				mins = Integer.parseInt(s);

			s = m.group(DURATION_SECONDS);
			if (s != null && !s.equals(""))
				secs = Integer.parseInt(s);
		}
	}

	/**
	 * @param years2
	 * @param months2
	 * @param days2
	 * @param hours2
	 * @param mins2
	 * @param secs2
	 */
	public Duration(int years2, int months2, int days2, int hours2,	int mins2, int secs2) {
			years 	= years2;
			months 	= months2;
			days 	= days2;
			hours	= hours2;
			mins 	= mins2;
			secs 	= secs2;
	}

	public String toString() {

		StringBuilder sb = new StringBuilder();
		if (years > 0 || months > 0 || days > 0 || hours > 0 || mins > 0
				|| secs > 0) {
			sb.append("P");
		} else {
			return "P0D";
		}

		if (years > 0)
			sb.append(years + "Y");

		if (months > 0)
			sb.append(months + "M");

		if (days > 0)
			sb.append(days + "D");

		if (hours > 0 || mins > 0 || secs > 0) {
			sb.append("T");
		} else {
			return sb.toString();
		}

		if (hours > 0)
			sb.append(hours + "H");
		if (mins > 0)
			sb.append(mins + "M");
		if (secs > 0)
			sb.append(secs + "S");

		return sb.toString();
	}

	public void addToCalendar(Calendar cal) {
		if(years>0)
			cal.add(Calendar.YEAR, years);

		if(months>0)
			cal.add(Calendar.MONTH, months);

		if(days>0)
			cal.add(Calendar.DATE, days);
		
		if(hours>0)
			cal.add(Calendar.HOUR_OF_DAY, hours);
		
		if(mins>0)
			cal.add(Calendar.MINUTE, mins);
		
		if(secs>0)
			cal.add(Calendar.SECOND, secs);
	}
	
	/**
	 * @return the years
	 */
	public int getYears() {
		return years;
	}

	/**
	 * @return the months
	 */
	public int getMonths() {
		return months;
	}

	/**
	 * @return the days
	 */
	public int getDays() {
		return days;
	}

	/**
	 * @return the hours
	 */
	public int getHours() {
		return hours;
	}

	/**
	 * @return the mins
	 */
	public int getMins() {
		return mins;
	}

	/**
	 * @return the secs
	 */
	public int getSecs() {
		return secs;
	}


}
