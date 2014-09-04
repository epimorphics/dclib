/******************************************************************
 * File:        CalendarBase.java
 * Created by:  skw
 * Created on:  23 Aug 2010
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
 * $Id:  $
 *****************************************************************/
package com.epimorphics.govData.util;

import java.util.Calendar;
import java.util.Locale;

import com.epimorphics.govData.vocabulary.INTERVALS;
import com.epimorphics.govData.vocabulary.TIME;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * @author skw
 *
 */
public class CalendarBase {
	
	static final String CALENDAR_NAME		= "British";
	static final String CALENDAR_BASE_URI	= "http://reference.data.gov.uk/";  

	static final String WEEK_PREFIX			= "-W";
	static final String MONTH_PREFIX		= "-";
	static final String DAY_PREFIX			= "-";
	
	static final String WEEK_ID_STEM		= "id/week/";
	static final String DAY_ID_STEM			= "id/day/";
	static final String YEAR_ID_STEM		= "id/year/";
	static final String INSTANT_ID_STEM		= "id/gregorian-instant/";
	
	static final protected Literal oneWeek 	= ResourceFactory.createTypedLiteral("P7D", XSDDatatype.XSDduration);
	static final protected Literal oneDay 	= ResourceFactory.createTypedLiteral("P1D", XSDDatatype.XSDduration);
	static final protected Literal oneYear 	= ResourceFactory.createTypedLiteral("P1Y", XSDDatatype.XSDduration);
	
	protected Resource thisResource;
	
	/**
	 * @return
	 */
	public Resource getResource() {
		return thisResource;
	}

	/**
	 * Add Ordinal fields to the current temporal entity.
	 * 
	 */
	protected static void addCalendarOrdinals(Resource res, int year, int moy, int dom, int hod, int moh, int som ) {
		res.addProperty(INTERVALS.ordinalSecondOfMinute, Integer.toString(som), XSDDatatype.XSDinteger);
		addCalendarOrdinals(res, year, moy, dom, hod, moh );
	}
	
	protected static void addCalendarOrdinals(Resource res, int year, int moy, int dom, int hod, int moh) {
		res.addProperty(INTERVALS.ordinalMinuteOfHour,   Integer.toString(moh), XSDDatatype.XSDinteger);
		addCalendarOrdinals(res, year, moy, dom, hod );
	}

	protected static void addCalendarOrdinals(Resource res, int year, int moy, int dom, int hod) {
		res.addProperty(INTERVALS.ordinalHourOfDay,   Integer.toString(hod), XSDDatatype.XSDinteger);
		addCalendarOrdinals(res, year, moy, dom);
	}
	
	protected static void addCalendarWoyOrdinals(Resource res, int year, int woy) {
		res.addProperty(INTERVALS.ordinalWeekOfYear,     Integer.toString(woy), XSDDatatype.XSDinteger);
		res.addProperty(INTERVALS.ordinalWeekOfYearYear, Integer.toString(year), XSDDatatype.XSDinteger); 
	}
	
	protected static void addCalendarOrdinals(Resource res, int year, int moy, int dom) {
		res.addProperty(INTERVALS.ordinalDayOfMonth, 	  Integer.toString(dom), XSDDatatype.XSDinteger);

		GregorianOnlyCalendar cal = new GregorianOnlyCalendar(Locale.UK);
		cal.set(year, moy-1,dom);
		int woy = cal.get(Calendar.WEEK_OF_YEAR);
		int dow = CalendarUtils.calendarDayToOrdinalDay(cal.get(Calendar.DAY_OF_WEEK));
		int doy =  cal.get(Calendar.DAY_OF_YEAR);
		int woy_year = CalendarUtils.getWeekOfYearYear(cal);

		addCalendarWoyOrdinals(res, woy_year, woy);

		res.addProperty(INTERVALS.ordinalDayOfWeek,      Integer.toString(dow), XSDDatatype.XSDinteger);
		res.addProperty(INTERVALS.dayOfWeek,     		  CalendarUtils.ordinalDayOfWeekToDayOfWeek(dow));
		res.addProperty(INTERVALS.ordinalDayOfYear,      Integer.toString(doy), XSDDatatype.XSDinteger);

		addCalendarOrdinals(res, year, moy);
	}

	protected static void addCalendarOrdinals(Resource res, int year, int moy) {
//		int hoy = ((moy-1)/6)+1;
		int qoy = ((moy-1)/3)+1;
		res.addProperty(INTERVALS.ordinalMonthOfYear,	  Integer.toString(moy), XSDDatatype.XSDinteger);
		res.addProperty(INTERVALS.monthOfYear,	  		  CalendarUtils.ordinalMonthOfYearToMonthOfYear(moy));
		
		addCalendarQoyOrdinals(res, year, qoy);
	}

	protected static void addCalendarQoyOrdinals(Resource res, int year, int qoy) {
		res.addProperty(INTERVALS.ordinalQuarterOfYear,     Integer.toString(qoy), XSDDatatype.XSDinteger);
		addCalendarHoyOrdinals(res, year, ((qoy-1)/2)+1);
	}	
	
	protected static void addCalendarHoyOrdinals(Resource res, int year, int hoy) {
		res.addProperty(INTERVALS.ordinalHalfOfYear,     Integer.toString(hoy), XSDDatatype.XSDinteger);
		addCalendarOrdinals(res, year);
	}
	
	protected static void addCalendarOrdinals(Resource res, int year) {
		res.addProperty(INTERVALS.ordinalYear, Integer.toString(year), XSDDatatype.XSDinteger);
	}
	
	protected static void connectToNeigbours(Model model, Resource r_this, Resource r_next, Resource r_prev) {
			model.add(r_this, INTERVALS.nextInterval, r_next);
			model.add(r_this, TIME.intervalMeets, r_next);
			model.add(r_next, TIME.intervalMetBy, r_this);

			model.add(r_this, INTERVALS.previousInterval, r_prev);
			model.add(r_this, TIME.intervalMetBy, r_prev);
			model.add(r_prev, TIME.intervalMeets, r_this);
		}
	
	static protected void setDayOfWeek(Model m, Resource r_day, int i_dow) {
		Resource r_dow = null;
		switch (i_dow) {
		case Calendar.MONDAY:
			r_dow = TIME.Monday;
			break;
		case Calendar.TUESDAY:
			r_dow = TIME.Tuesday;
			break;
		case Calendar.WEDNESDAY:
			r_dow = TIME.Wednesday;
			break;
		case Calendar.THURSDAY:
			r_dow = TIME.Thursday;
			break;
		case Calendar.FRIDAY:
			r_dow = TIME.Friday;
			break;
		case Calendar.SATURDAY:
			r_dow = TIME.Saturday;
			break;
		case Calendar.SUNDAY:
			r_dow = TIME.Sunday;
			break;
		}
		if (r_dow != null)
			m.add(r_day, TIME.dayOfWeek, r_dow);
	}
	
	static public String getDecimalSuffix(int dom) {
		dom = dom % 100;
		return (((dom != 11) && ((dom % 10) == 1)) ? "st" :
			    ((dom != 12) && ((dom % 10) == 2)) ? "nd" :
			    ((dom != 13) && ((dom % 10) == 3)) ? "rd" : "th");
	}

}
