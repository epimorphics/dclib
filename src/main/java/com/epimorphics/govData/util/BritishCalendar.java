/******************************************************************
 * File:        BritishCalendar.java
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
import java.util.TimeZone;

public class BritishCalendar extends GregorianCalendar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2900153523250912600L;

	private static GregorianCalendar changeDate = new GregorianCalendar(1752, Calendar.SEPTEMBER, 14, 0, 0, 0);

	public BritishCalendar() {
		super();
		setGregorianChangeDate();
		//Zero the milliseconds as well.
		set(Calendar.MILLISECOND, 0);

	}
	
	public BritishCalendar(Locale locale) {
		super(locale);
		setGregorianChangeDate();
		//Zero the milliseconds as well.
		set(Calendar.MILLISECOND, 0);

	}

	public BritishCalendar(TimeZone zone) {
		super(zone);
		setGregorianChangeDate();
		//Zero the milliseconds as well.
		set(Calendar.MILLISECOND, 0);

	}

	public BritishCalendar(TimeZone zone, Locale locale) {
		super(zone, locale);
		setGregorianChangeDate();
		//Zero the milliseconds as well.
		set(Calendar.MILLISECOND, 0);
	}

	public BritishCalendar(int year, int month, int dayOfMonth) {
		super();
		setGregorianChangeDate();
		set(year, month, dayOfMonth);
		//Zero the milliseconds as well.
		set(Calendar.MILLISECOND, 0);

	}

	public BritishCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
		super();
		setGregorianChangeDate();
		set(year, month, dayOfMonth, hourOfDay, minute);
		//Zero the milliseconds as well.
		set(Calendar.MILLISECOND, 0);
	}

	public BritishCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
		super();
		setGregorianChangeDate();
		set(year, month, dayOfMonth, hourOfDay, minute, second);
		//Zero the milliseconds as well.
		set(Calendar.MILLISECOND, 0);
	}
	
	private void setGregorianChangeDate() {
		this.setGregorianChange(changeDate.getTime());		
	}
	
	public Object clone() {
		BritishCalendar res  = (BritishCalendar) super.clone();
		setGregorianChangeDate();
			
		return res;
	}
}
