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

import joinery.DataFrame.Function;


public class BooleanConverter implements Function<Object, Boolean> {

	@Override
	public Boolean apply(final Object value) {
		final String str = String.valueOf(value);
		if (str.matches("t(r(u(e)?)?)?|y(e(s)?)?")) {
			return new Boolean(true);
		} else if (str.matches("f(a(l(s(e)?)?)?)?|n(o)?")) {
			return new Boolean(false);
		}
		return null;
	}
}
