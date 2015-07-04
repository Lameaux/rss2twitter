package com.euromoby.r2t.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	private SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss"); 	
	public String format(long millis) {
		return dt.format(new Date(millis));
	}
}
