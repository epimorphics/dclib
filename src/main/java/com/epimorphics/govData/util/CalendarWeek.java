/******************************************************************
 * File:        CalendarWeek.java
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
import com.epimorphics.vocabs.SKOS;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author skw
 *
 */
public class CalendarWeek extends CalendarInterval {
	
	private CalendarWeek nextWeek		= null;
	private CalendarWeek lastWeek		= null;

	public CalendarWeek(Model m, int year, int woy, boolean withNeighbours, boolean withoutLabels) {
		Resource r_week = createResourceAndLabels(m, year, woy, withoutLabels);
		thisResource = r_week;

		BritishCalendar cal = new BritishCalendar(Locale.UK);
		CalendarUtils.setWeekOfYear(year, woy , cal);

		if(withNeighbours) {
			addNeighboringIntervals(m, r_week, cal, withoutLabels);
		}

		if(m.contains(r_week,TIME.hasDurationDescription))
			return;
		
		addCalendarWoyOrdinals(r_week, year, woy);
		
		
//		addCalendarOrdinals(r_week, cal.get(Calendar.YEAR));

		r_week.addProperty(INTERVALS.hasXsdDurationDescription, oneWeek);
		r_week.addProperty(TIME.hasDurationDescription, INTERVALS.one_week );
//		r_week.addProperty(SCOVO.min, CalendarUtils.formatScvDate(cal), XSDDatatype.XSDdate);

		startInstant = new CalendarInstant(m, cal, withoutLabels);	
		m.add(r_week, TIME.hasBeginning, startInstant.getResource());
		cal.add(Calendar.DATE, 7);
		endInstant = new CalendarInstant(m, cal, withoutLabels);	
		m.add(r_week, TIME.hasEnd, endInstant.getResource());
		
//		cal.add(Calendar.SECOND, -1);
//		m.add(r_week, SCOVO.max, CalendarUtils.formatScvDate(cal), XSDDatatype.XSDdate);
		
	}

	private Resource createResourceAndLabels(Model m, int year, int woy, boolean withoutLabels) {
		String relPart = String.format("%04d",year) + WEEK_PREFIX + String.format("%02d", woy);
		
		String s_weekURI = CALENDAR_BASE_URI + WEEK_ID_STEM + relPart;

		Resource r_week = m.createResource(s_weekURI, INTERVALS.Iso8601Week);
		r_week.addProperty(RDF.type,INTERVALS.Week);
		
		if(!withoutLabels && !m.contains(r_week, RDFS.label)) {
			String s_label = "British Week:" + relPart;
			r_week.addProperty(SKOS.prefLabel, s_label, "en");
			r_week.addProperty(RDFS.label, s_label, "en");
			r_week.addProperty(RDFS.comment, "Week " + woy + " of the "+CALENDAR_NAME+" calendar year " + String.format("%04d",year) );
		}
		return r_week;
	}

	private void addNeighboringIntervals(Model model, Resource r_week, BritishCalendar bcal, boolean withoutLabels) {
		long ts = bcal.getTimeInMillis();
		
		bcal.add(Calendar.DATE,7);
		nextWeek = new CalendarWeek(model, CalendarUtils.getWeekOfYearYear(bcal),bcal.get(Calendar.WEEK_OF_YEAR), false, withoutLabels);
		bcal.setTimeInMillis(ts);
		bcal.add(Calendar.DATE,-7);
		lastWeek = new CalendarWeek(model, CalendarUtils.getWeekOfYearYear(bcal) ,bcal.get(Calendar.WEEK_OF_YEAR), false, withoutLabels);
		bcal.setTimeInMillis(ts);

		// Link adjacent months
		connectToNeigbours(model, r_week, nextWeek.getResource(), lastWeek.getResource());
	}
	

	/**
	 * @return the nextWeek
	 */
	public CalendarWeek getNextWeek() {
		return nextWeek;
	}

	/**
	 * @return the lastWeek
	 */
	public CalendarWeek getLastWeek() {
		return lastWeek;
	}
}
