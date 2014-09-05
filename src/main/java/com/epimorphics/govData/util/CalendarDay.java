/******************************************************************
 * File:        CalendarDay.java
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
public class CalendarDay extends CalendarInterval {
	
	public CalendarDay(Model m, BritishCalendar bcal, boolean withoutLabels) {
		int year = bcal.get(Calendar.YEAR);
		int moy  = bcal.get(Calendar.MONTH)-Calendar.JANUARY+1;
		int dom	 = bcal.get(Calendar.DAY_OF_MONTH);
		Resource r_day = createResourceAndLabels(m, year, moy, dom, withoutLabels);
		thisResource = r_day;
		
		// Return if we have done this already.
		if(m.contains(r_day,TIME.hasDurationDescription))
			return;

//		m.add(r_day, RDF.type, SCOVO.Dimension);
		BritishCalendar cal = new BritishCalendar(Locale.UK);
		cal.setLenient(false);
		cal.set(year, moy-1, dom,0 , 0, 0);
		cal.getTimeInMillis();
		
		addCalendarOrdinals(r_day, year, moy, dom);
		
		m.add(r_day, INTERVALS.hasXsdDurationDescription, oneDay);
		m.add(r_day, TIME.hasDurationDescription, INTERVALS.one_day);
//		m.add(r_day, SCOVO.min, CalendarUtils.formatScvDateTime(cal), XSDDatatype.XSDdateTime);


		startInstant = new CalendarInstant(m, cal, withoutLabels);	
		m.add(r_day, TIME.hasBeginning, startInstant.getResource());

		int i_dow = cal.get(Calendar.DAY_OF_WEEK);
		setDayOfWeek(m, r_day, i_dow);

		cal.add(Calendar.DAY_OF_MONTH,1);
		endInstant = new CalendarInstant(m, cal, withoutLabels);	
		m.add(r_day, TIME.hasEnd, endInstant.getResource());
		
		cal.add(Calendar.SECOND, -1);
//		m.add(r_day, SCOVO.max, CalendarUtils.formatScvDateTime(cal), XSDDatatype.XSDdateTime);
		
	}

	public static Resource createResourceAndLabels(Model m, int year, int moy, int dom, boolean withoutLabels) {
		String relPart = String.format("%04d",year) + MONTH_PREFIX + String.format("%02d", moy)
				+ DAY_PREFIX + String.format("%02d", dom);
	
		String s_dayURI = CALENDAR_BASE_URI + DAY_ID_STEM + relPart;
		
		Resource r_day = m.createResource(s_dayURI, INTERVALS.CalendarDay);
		r_day.addProperty(RDF.type, INTERVALS.Day);
	
		if(!withoutLabels && !m.contains(r_day,RDFS.label)) {
			String s_label = ""+CALENDAR_NAME+" Day:" + relPart;
			m.add(r_day, SKOS.prefLabel, s_label, "en");
			m.add(r_day, RDFS.label, s_label, "en");
	
			BritishCalendar cal = new BritishCalendar(Locale.UK);
			cal.set(year, moy - 1, dom);
	
			String s_month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG,Locale.UK);
			String s_dayOfWeek = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
			String s_domSuffix = getDecimalSuffix(dom);
			m.add(r_day, RDFS.comment, s_dayOfWeek + " the " + dom + s_domSuffix + " of " + s_month + " in the "+CALENDAR_NAME+" calendar year " + String.format("%04d",year) , "en");
		}
		return r_day;
	}
}
