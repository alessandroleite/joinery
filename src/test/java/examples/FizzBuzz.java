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
package examples;

import java.util.Arrays;
import java.util.List;

import joinery.DataFrame;

public class FizzBuzz {
    public static void main(final String[] args) {
        // generate data frame with numbers 1-100
        final DataFrame input = new DataFrame().add("number");
        for (int i = 1; i <= 100; i++) {
            input.append(Arrays.asList(i));
        }

        // apply transform to "solve" fizz buzz
        final DataFrame df = input
                .add("value")
                .transform(new DataFrame.RowFunction<Integer, Object>() {
                    @Override
                    public List<List<Object>> apply(final List<Integer> row) {
                        final int value = row.get(0);
                        return Arrays.asList(
                            Arrays.<Object>asList(
                                value,
                                value % 15 == 0 ? "FizzBuzz" :
                                value %  3 == 0 ? "Fizz" :
                                value %  5 == 0 ? "Buzz" :
                                String.valueOf(value)
                            )
                        );
                    }
                });

        // group, count, sort, and display the top results
        System.out.println(
                df.groupBy("value")
                    .count()
                    .sortBy("-number")
                    .head(3)
                    .resetIndex()
            );
    }
}
