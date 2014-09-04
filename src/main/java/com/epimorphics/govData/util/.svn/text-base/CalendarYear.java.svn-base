/******************************************************************
 * File:        CalendarYear.java
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

import com.epimorphics.govData.vocabulary.INTERVALS;
import com.epimorphics.govData.vocabulary.SKOS;
import com.epimorphics.govData.vocabulary.TIME;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author skw
 *
 */
public class CalendarYear extends CalendarInterval {
	
	private int year;
	private CalendarYear nextYear=null;
	private CalendarYear lastYear=null;
	
	public CalendarYear(Model model, int year, boolean withNeighbours, boolean withoutLabels) {
		Resource r_year = createResourceAndLabels(model, year, withoutLabels);
		
		thisResource = r_year;
		this.year = year;

		if(withNeighbours) {
			addNeighboringIntervals(model, r_year, withoutLabels);
		}
		//Return if we have done all this already
		if(model.contains(r_year, INTERVALS.hasXsdDurationDescription))
			return;
		
//		model.add(r_year, RDF.type, SCOVO.Dimension);
		BritishCalendar cal = new BritishCalendar(year, Calendar.JANUARY, 1, 0, 0, 0);	
		cal.setLenient(false);
		
		addCalendarOrdinals(r_year, year);
		
		r_year
			.addProperty(INTERVALS.hasXsdDurationDescription, oneYear)
			.addProperty(TIME.hasDurationDescription, INTERVALS.one_year);
		
		startInstant = new CalendarInstant(model, cal, withoutLabels);	
		model.add(r_year, TIME.hasBeginning, startInstant.getResource());

//		model.add(r_year, SCOVO.min, CalendarUtils.formatScvDateLiteral(cal));	
		
		cal.add(Calendar.YEAR, 1);
		endInstant = new CalendarInstant(model, cal, withoutLabels);	
		model.add(r_year, TIME.hasEnd, endInstant.getResource());

		cal.add(Calendar.SECOND, -1);
//		model.add(r_year, SCOVO.max, CalendarUtils.formatScvDateLiteral(cal));	

	}
	
	Resource createResourceAndLabels(Model model, int year, boolean withoutLabels) {
		String s_yearURI = CALENDAR_BASE_URI+ YEAR_ID_STEM + String.format("%04d",year);

		Resource r_year = model.createResource(s_yearURI, INTERVALS.CalendarYear);

		r_year.addProperty(RDF.type, INTERVALS.Year);
		
		if(!withoutLabels && !model.contains(r_year, RDFS.label)) {
			String s_label = ""+CALENDAR_NAME+" Year:" + String.format("%04d",year) ;
			model.add(r_year, SKOS.prefLabel, s_label, "en");
			model.add(r_year, RDFS.label, s_label, "en");
			model.add(r_year, RDFS.comment, "The "+CALENDAR_NAME+" calendar year of " + String.format("%04d",year) , "en");
		}

		return r_year;
	}
	
	private void addNeighboringIntervals(Model model, Resource r_year, boolean withoutLabels) {
		nextYear = new CalendarYear(model, year+1, false, withoutLabels);
		lastYear = new CalendarYear(model, year-1, false, withoutLabels);

		// Link adjacent months
		connectToNeigbours(model, r_year, nextYear.getResource(), lastYear.getResource());
	}

	/**
	 * @return
	 */
	public int getYear() {
		return year;
	}

}
