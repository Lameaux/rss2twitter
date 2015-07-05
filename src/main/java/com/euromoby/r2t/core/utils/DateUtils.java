package com.euromoby.r2t.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

	private SimpleDateFormat dt;

	public DateUtils() {
		dt = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss zzz");
		dt.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public String toGMT(long millis) {
		return dt.format(new Date(millis));
	}
}
