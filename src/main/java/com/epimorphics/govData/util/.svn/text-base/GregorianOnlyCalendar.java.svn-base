/******************************************************************
 * File:        GregorianOnlyCalendar.java
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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class GregorianOnlyCalendar extends GregorianCalendar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6297956560693049540L;
	
//	private static GregorianCalendar changeDate = new GregorianCalendar(1752, Calendar.SEPTEMBER, 14, 0, 0, 0);

	public GregorianOnlyCalendar() {
		super();
		setGregorianChangeDate();
		//Zero the milliseconds as well.
		set(Calendar.MILLISECOND, 0);
	}
	
	public GregorianOnlyCalendar(Locale locale) {
		super(locale);
		setGregorianChangeDate();
		//Zero the milliseconds as well.
		set(Calendar.MILLISECOND, 0);
	}

	public GregorianOnlyCalendar(TimeZone zone) {
		super(zone);
		setGregorianChangeDate();
		//Zero the milliseconds as well.
		set(Calendar.MILLISECOND, 0);
	}

	public GregorianOnlyCalendar(TimeZone zone, Locale locale) {
		super(zone, locale);
		setGregorianChangeDate();
		//Zero the milliseconds as well.
		set(Calendar.MILLISECOND, 0);
	}

	public GregorianOnlyCalendar(int year, int month, int dayOfMonth) {
		super();
		setGregorianChangeDate();
		set(year, month, dayOfMonth);
		//Zero the milliseconds as well.
		set(Calendar.MILLISECOND, 0);
	}

	public GregorianOnlyCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
		super();
		setGregorianChangeDate();
		set(year, month, dayOfMonth, hourOfDay, minute, 0);
		//Zero the milliseconds as well.
		set(Calendar.MILLISECOND, 0);
	}

	public GregorianOnlyCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
		super();
		setGregorianChangeDate();
		set(year, month, dayOfMonth, hourOfDay, minute, second);
		//Zero the milliseconds as well.
		set(Calendar.MILLISECOND, 0);
	}
	
	private void setGregorianChangeDate() {
//		this.setGregorianChange(changeDate.getTime());		
		this.setGregorianChange(new Date(Long.MIN_VALUE));		
	}

	/**
	 * @param gcEnd
	 * @return
	 */
	public Duration getDurationTo(Calendar other) {
		long start = getTimeInMillis();
		long end = other.getTimeInMillis();
		GregorianOnlyCalendar working = (GregorianOnlyCalendar) this.clone();
		if(start==end) 
			return null;
		
		if(start>end) {
			return null;
		}
		int years 		= advanceFieldUptoLimit(Calendar.YEAR, end, working);
		int months		= advanceFieldUptoLimit(Calendar.MONTH, end, working);
		int days		= advanceFieldUptoLimit(Calendar.DATE, end, working);
		int hours		= advanceFieldUptoLimit(Calendar.HOUR_OF_DAY, end, working);
		int mins		= advanceFieldUptoLimit(Calendar.MINUTE, end, working);;
		int secs		= advanceFieldUptoLimit(Calendar.SECOND, end, working);;

		return new Duration(years, months, days, hours, mins, secs);
	}
	
	private int advanceFieldUptoLimit(int calendar_field, long limit, GregorianOnlyCalendar working) {
		int count = 0;
		
		if (working.getTimeInMillis() >= limit)
			return 0;
		
		while (working.getTimeInMillis()< limit) {
			count++;
			working.add(calendar_field, 1);
			if(working.getTimeInMillis()==limit) {
				return count;
			}
		}
		// Back out of a step too far
		working.add(calendar_field, -1);
		return --count;
	}
	
	public Object clone() {
		GregorianOnlyCalendar res  = (GregorianOnlyCalendar) super.clone();
		setGregorianChangeDate();	
		return res;
	}
}
