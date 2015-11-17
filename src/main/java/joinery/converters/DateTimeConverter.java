/**
 *    Joinery - Data frames for Java
 *    Copyright (c) 2014, 2015 IBM Corp.
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package joinery.converters;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import joinery.DataFrame.Function;

public class DateTimeConverter implements Function<Object, Date> {
	
	private final List<DateFormat> formats = Arrays.<DateFormat>asList(
            new SimpleDateFormat("y-M-d'T'HH:mm:ssXXX"),
            new SimpleDateFormat("y-M-d'T'HH:mm:ssZZZ"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
            new SimpleDateFormat("y-M-d"),
            new SimpleDateFormat("y-M-d hh:mm a"),
            new SimpleDateFormat("y-M-d HH:mm"),
            new SimpleDateFormat("y-M-d hh:mm:ss a"),
            new SimpleDateFormat("y-M-d HH:mm:ss"),
            new SimpleDateFormat("y/M/d hh:mm:ss a"),
            new SimpleDateFormat("y/M/d HH:mm:ss"),
            new SimpleDateFormat("y/M/d hh:mm a"),
            new SimpleDateFormat("y/M/d HH:mm"),
            new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a"),
            new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSSSSSSSS a"),
            new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy"),
            DateFormat.getDateTimeInstance(),
            new SimpleDateFormat("y/M/d"),
            new SimpleDateFormat("M/d/y hh:mm:ss a"),
            new SimpleDateFormat("M/d/y HH:mm:ss"),
            new SimpleDateFormat("M/d/y hh:mm a"),
            new SimpleDateFormat("M/d/y HH:mm"),
            new SimpleDateFormat("M/d/y"),
            DateFormat.getDateInstance()
        );


	@Override
	public Date apply(final Object value) {
		final String source = String.valueOf(value);
		final ParsePosition pp = new ParsePosition(0);
		
		for (final DateFormat format : formats) {
			final Date dt = format.parse(source, pp);
			if (pp.getIndex() == source.length()) {
				return dt;
			}
			pp.setIndex(0);
			pp.setErrorIndex(-1);
		}
		return null;
	}
}
