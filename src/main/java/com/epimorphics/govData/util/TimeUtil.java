/******************************************************************
 * File:        TimeUtil.java
 * Created by:  Dave Reynolds
 * Created on:  18 Oct 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.govData.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Utility to parse dates and datetimes based on skw conversion code.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TimeUtil {
    
    static {
        Locale.setDefault(Locale.UK);
    }

    final static SimpleDateFormat[] DT_FORMAT = { 
            makeFmt("dd/MM/yyyy HH:mm"), 
            makeFmt("yyyyMMddHHmmss"),
            makeFmt("dd/MM/yyyyHH:mm") };
    
    final static SimpleDateFormat[] ISO_FORMAT = { 
            makeFmt("yyyy-MM-dd'T'HH:mm:ss"), 
            makeFmt("yyyy-MM-dd'T'HH:mm:ss"), 
            makeFmt("dd/MM/yyyy'T'HH:mm:ss"), 
            makeFmt("dd/MM/yyyy'T'HH:mm:ss") }; 
    
    final static SimpleDateFormat[] DATE_FORMAT = { 
            makeFmt("dd/MM/yyyy"), 
            makeFmt("dd-MM-yyyy"),
            makeFmt("dd-MMM-yy"), 
            makeFmt("dd-MMM-yyyy") };
    
    final static SimpleDateFormat makeFmt(String format) {
        SimpleDateFormat fmt = new SimpleDateFormat(format, Locale.UK);
        fmt.setTimeZone( TimeZone.getTimeZone("UTC+1") );   // Force BST for incoming data
        return fmt;
    }
    
    public static BritishCalendar parseDateTime(String s_fdateTime) throws ParseException {
        return doParseDateTime(s_fdateTime, DT_FORMAT);
    }

    public static BritishCalendar parseISODateTime(String s_fdateTime) throws ParseException {
        return doParseDateTime(s_fdateTime, ISO_FORMAT);
    }

    public static BritishCalendar parseDate(String s_date) throws ParseException {
        return doParseDateTime(s_date, DATE_FORMAT);
    }
    
    public synchronized static BritishCalendar doParseDateTime(String s_fdateTime, SimpleDateFormat[] checks) throws ParseException {
        BritishCalendar bcal = new BritishCalendar(Locale.UK);
        bcal.setTimeZone( TimeZone.getTimeZone("UTC+1") );   // Force BST internally
        
        ParseException lastException = null;
        // Sort out some property values from the sample date/time
        for (int i = 0; i < checks.length; i++) {
            Date date;
            try {
                date = checks[i].parse(s_fdateTime);
            } catch (ParseException e) {
                lastException = e;
                continue;
            }
            bcal.setTime(date);
            return bcal;
        }
        throw lastException;
    }
    
    public static BritishCalendar addHours(BritishCalendar base, int hours, int minutes) {
        BritishCalendar dt = new BritishCalendar(Locale.UK);
        dt.setTime(base.getTime());
        dt.add(Calendar.HOUR, hours);
        dt.add(Calendar.MINUTE, minutes);
        return dt;
    }
}
