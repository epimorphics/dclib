/******************************************************************
 * File:        CalendarInstant.java
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

import com.epimorphics.govData.vocabulary.TIME;
import com.epimorphics.vocabs.SKOS;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDFS;

/**
 * 
 * @author skw
 *
 */
public class CalendarInstant extends CalendarBase {
	
	public CalendarInstant(Model model, Calendar cal, boolean withoutLabels) {
		GregorianOnlyCalendar cal2 = new GregorianOnlyCalendar(Locale.UK);
		cal2.setTimeInMillis(cal.getTimeInMillis());
		String s_relPart = CalendarUtils.toXsdDateTime(cal2);

		String s_instURI = CALENDAR_BASE_URI + INSTANT_ID_STEM + s_relPart;
		if(model.containsResource(ResourceFactory.createResource(s_instURI))) {
			thisResource = model.createResource(s_instURI);
			return;
		}
		Resource r_inst = model.createResource(s_instURI, TIME.Instant);
		Literal l_dateTime = ResourceFactory.createTypedLiteral(s_relPart, XSDDatatype.XSDdateTime);
		thisResource = r_inst;
		
		if(model.contains(r_inst, TIME.inXSDDateTime))
			return;
		
		int year = cal2.get(Calendar.YEAR);
		int moy  = cal2.get(Calendar.MONTH)+1-Calendar.JANUARY;
		int dom  = cal2.get(Calendar.DATE);
		int hod  = cal2.get(Calendar.HOUR_OF_DAY);
		int moh  = cal2.get(Calendar.MINUTE);
		int som  = cal2.get(Calendar.SECOND);
		
		addCalendarOrdinals(r_inst, year, moy, dom, hod, moh, som);

		if(!withoutLabels && !model.contains(r_inst, RDFS.label)) {
			String s_month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.UK);
			String s_dayOfWeek = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
			String s_domSuffix = getDecimalSuffix(dom);
			String s_hodSuffix = getDecimalSuffix(hod+1);
			String s_mohSuffix = getDecimalSuffix(moh+1);
			String s_somSuffix = getDecimalSuffix(som+1);
	
			model.add(r_inst, RDFS.comment, "The instant at start of the " + (som+1) + s_somSuffix + " second of " + (moh+1)
					+ s_mohSuffix + " minute of " + (hod+1) + s_hodSuffix + " hour of "
					+ s_dayOfWeek + " the " + dom + s_domSuffix + " " + s_month
					+ " of the "+CALENDAR_NAME+" calendar year " + String.format("%04d",year) , "en");
				                    
			model.add(r_inst, RDFS.label, ""+CALENDAR_NAME+" Instant:"+s_relPart, "en");
			model.add(r_inst, SKOS.prefLabel, ""+CALENDAR_NAME+" Instant:"+s_relPart, "en");
		}
		model.add(r_inst, TIME.inXSDDateTime, l_dateTime);
	}
}
