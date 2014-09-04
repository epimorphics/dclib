/******************************************************************
 * File:        CalendarInterval.java
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

/**
 * @author skw
 *
 */
public class CalendarInterval extends CalendarBase {
	
	
	protected CalendarInstant startInstant = null;
	protected CalendarInstant endInstant 	= null;
	
	/**
	 * @return the r_startInstant
	 */
	public CalendarInstant get_startInstant() {
		return startInstant;
	}

	/**
	 * @return the r_endInstant
	 */
	public CalendarInstant get_endInstant() {
		return endInstant;
	}

}
