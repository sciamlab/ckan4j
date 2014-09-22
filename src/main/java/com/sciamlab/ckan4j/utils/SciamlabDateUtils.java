package com.sciamlab.ckan4j.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author SciamLab
 *
 */

public class SciamlabDateUtils {

	private static final DateFormat ISO8061_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	public static Date getDateFromIso8061DateString(String dateString) {
		try {
			return ISO8061_FORMATTER.parse(dateString);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getCurrentDateAsIso8061String() {
		return ISO8061_FORMATTER.format(new Date());
	}

	public static String getDateAsIso8061String(Date date) {
		return ISO8061_FORMATTER.format(date);
	}

	public static String getCurrentDateAsFormattedString(String format) {
		return new SimpleDateFormat(format).format(new Date());
	}
	
	public static String getDateAsFormattedString(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}
}
