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


import java.util.Arrays;
import java.util.List;

import joinery.DataFrame.Function;
import joinery.DataFrame.RowFunction;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class DataFrameViewsTest {
    private DataFrame df;

    @Before
    public void setUp()
    throws Exception {
        df = new DataFrame("one", "two", "three")
                .append(Arrays.asList("a", null, "c"))
                .append(Arrays.asList("aa", "bb", "cc"));
    }

    @Test
    public void testApply() {
        assertArrayEquals(
                new Object[] { 1, 2, 0, 2, 1, 2 },
                df.apply(new Function<Object, Integer>() {
                    @Override
                    public Integer apply(final Object value) {
                        return value == null ? 0 : value.toString().length();
                    }
                }).toArray()
            );
    }

    @Test
    public void testTransform() {
        assertArrayEquals(
                new Object[] {
                    "a", "a", "aa", "aa",
                    null, null, "bb", "bb",
                    "c", "c", "cc", "cc"
                },
                df.transform(new RowFunction<Object, Object>() {
                    @Override
                    public List<List<Object>> apply(final List<Object> values) {
                        return Arrays.asList(values, values);
                    }
                }).toArray()
            );
    }

    @Test
    public void testFillNa() {
        assertArrayEquals(
                new Object[] { "a", "aa", "b", "bb", "c", "cc" },
                df.fillna("b").toArray()
            );
    }
}
