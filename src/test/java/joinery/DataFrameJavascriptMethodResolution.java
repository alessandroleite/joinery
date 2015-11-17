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
package joinery;

import joinery.js.JavascriptExpressionSuite;
import joinery.js.JavascriptExpressionSuite.JavascriptResource;

import org.junit.runner.RunWith;

@RunWith(JavascriptExpressionSuite.class)
@JavascriptResource(name="expressions.js")
public class DataFrameJavascriptMethodResolution {
    /* see test/resources/expressions.js for javascript expressions tested
     * this is stupid and hard to maintain,
     * but I'm tired of ambiguous method exceptions from js...
     */
}
