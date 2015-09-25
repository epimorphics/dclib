/******************************************************************
 * File:        CalendarUtils.java
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
import java.util.GregorianCalendar;
import java.util.Locale;

import com.epimorphics.govData.vocabulary.INTERVALS;
import com.epimorphics.govData.vocabulary.TIME;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;



public class CalendarUtils {
	
//	public static final SimpleDateFormat iso8601dateTimeformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//	static final SimpleDateFormat iso8601gYearformat =    new SimpleDateFormat("yyyy");
//	static final SimpleDateFormat iso8601gYearMonthformat = new SimpleDateFormat("yyyy-MM");
//	public static final SimpleDateFormat iso8601dateformat = new SimpleDateFormat("yyyy-MM-dd");

	public static String toXsdDateTime(Calendar cal2) {
		GregorianOnlyCalendar cal = new GregorianOnlyCalendar(Locale.UK);
		cal.setLenient(false);
		cal.setTimeInMillis(cal2.getTimeInMillis());
	
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH)+1-Calendar.JANUARY;
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
//		String s2 = iso8601dateTimeformat.format(cal.getTimeInMillis());
		
		
		return String.format("%04d-%02d-%02dT%02d:%02d:%02d",year,month,day,hour,min,sec);
//		return iso8601dateTimeformat.format(cal.getTimeInMillis());
	}
	
	/*
	public static String toXsdDateTime(int yr, int moy, int dom, int hod, int moh, int som) {
		GregorianOnlyCalendar cal = new GregorianOnlyCalendar(yr, moy+Calendar.JANUARY-1, dom, hod, moh, som);
		cal.setLenient(false);
		return CalendarUtils.iso8601dateTimeformat.format(cal.getTime());
	}
	*/
//	public static Literal formatScvDate (Calendar cal2, SimpleDateFormat fmt, XSDDatatype type) {
//		GregorianOnlyCalendar cal = new GregorianOnlyCalendar(Locale.UK);
//		cal.setLenient(false);
//		cal.setTimeInMillis(cal2.getTimeInMillis());
//		return ResourceFactory.createTypedLiteral(formatScvDate(cal, fmt), type);		
//	}
//
//	public static String formatScvDate (Calendar cal2, SimpleDateFormat fmt) {		
//		GregorianOnlyCalendar cal = new GregorianOnlyCalendar(Locale.UK);
//		cal.setLenient(false);
//		cal.setTimeInMillis(cal2.getTimeInMillis());
//		return fmt.format(cal.getTimeInMillis());		
//	}

	/**
	 * @param cal
	 * @return
	 */
	public static String formatScvDate(Calendar cal2) {
		GregorianOnlyCalendar cal = new GregorianOnlyCalendar(Locale.UK);
		cal.setLenient(false);
		cal.setTimeInMillis(cal2.getTimeInMillis());
		
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH)+1-Calendar.JANUARY;
		int day = cal.get(Calendar.DATE);
		
		return String.format("%04d-%02d-%02d", year, month, day);
	}
	/**
	 * @param cal
	 * @return
	 */
	public static Literal formatScvDateLiteral(Calendar cal) {
		return ResourceFactory.createTypedLiteral(formatScvDate(cal), XSDDatatype.XSDdate);
	}

	/**
	 * @param cal
	 * @return
	 */
	public static String formatScvDateTime(Calendar cal) {
		return toXsdDateTime(cal);
	}
	/**
	 * @param cal
	 * @return
	 */
	public static Literal formatScvDateTimeLiteral(Calendar cal) {
		return ResourceFactory.createTypedLiteral(toXsdDateTime(cal), XSDDatatype.XSDdateTime);
	}

	public static int getWeekOfYearYear(GregorianCalendar cal) {
		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH)+1 - Calendar.JANUARY;
		int w = cal.get(Calendar.WEEK_OF_YEAR);
		
		if(w==1 && m==12)
			return y+1;
		
		if(w>50 && m==1)
			return y-1;
		
		return y;
	}
	
	/*
	public static boolean inCutOverAnomaly(GregorianCalendar cal) {
        int month = cal.get(Calendar.MONTH)+1-Calendar.JANUARY;
        int year = cal.get(Calendar.YEAR);
        int woy = cal.get(Calendar.WEEK_OF_YEAR);
        
        return (year == 1752 && woy>37);
	}
	
	public static boolean inCutOverAnomaly(int year, int woy) {
        return (year == 1752 && woy>37);
	}
	
	*/
	
	public static void setWeekOfYear(int year, int woy, GregorianCalendar cal) {
		boolean l = cal.isLenient();
		GregorianCalendar changeOver = new GregorianCalendar(Locale.UK);
		changeOver.setTime(cal.getGregorianChange());
		
		int changeYear =changeOver.get(Calendar.YEAR);
		
		cal.setLenient(true);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.WEEK_OF_YEAR, woy);
//		while (cal.get(Calendar.DAY_OF_WEEK)!= Calendar.MONDAY) {
//			cal.add(Calendar.DATE, -1);
//		}
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.getTimeInMillis();
        int cal_woy_week = cal.get(Calendar.WEEK_OF_YEAR);
        
        if(woy!=cal_woy_week &&
           year == changeYear &&
           woy-cal_woy_week == 1) {
        	cal.add(Calendar.DATE, 7);
        	cal_woy_week = cal.get(Calendar.WEEK_OF_YEAR);
        }
        
 		if(woy!=cal_woy_week) {
			throw new IllegalArgumentException("Invalid Week of Year: "+year+"-W"+woy);
		}
		cal.setLenient(l);
	}

	
	public static int calendarDayToOrdinalDay(int dow) {
		return dow==Calendar.MONDAY ? 1 :
			   dow==Calendar.TUESDAY ? 2 :
			   dow==Calendar.WEDNESDAY ? 3 :
			   dow==Calendar.THURSDAY ? 4 :
		       dow==Calendar.FRIDAY ? 5 :
		       dow==Calendar.SATURDAY ? 6 : 7;
	}

	/**
	 * @param dow
	 * @return
	 */
	private static final Resource days[] = 
	{
		TIME.Monday,
		TIME.Tuesday,
		TIME.Wednesday,
		TIME.Thursday,
		TIME.Friday,
		TIME.Saturday,
		TIME.Sunday		
	};
	public static Resource ordinalDayOfWeekToDayOfWeek(int dow) {
		if(1<=dow && dow<=7) {
			return days[dow-1];
		}
		return null;
	}

	/**
	 * @param moy
	 * @return
	 */
	private static final Resource months[] = 
	{ INTERVALS.January, 
	  INTERVALS.February,
	  INTERVALS.March,
	  INTERVALS.April,
	  INTERVALS.May,
	  INTERVALS.June,
	  INTERVALS.July,
	  INTERVALS.August,
	  INTERVALS.September,
	  INTERVALS.October,
	  INTERVALS.November,
	  INTERVALS.December
	};
	
	public static Resource ordinalMonthOfYearToMonthOfYear(int moy) {
		if(1<=moy && moy<=12) {
			return months[moy-1];
		}
		return null;
	}

}
