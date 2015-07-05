package com.euromoby.r2t.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

	private SimpleDateFormat dt;

	public DateUtils() {
		dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dt.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public String toGMT(long millis) {
		return dt.format(new Date(millis));
	}
}
