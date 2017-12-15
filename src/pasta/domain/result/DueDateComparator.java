/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package pasta.domain.result;

import java.util.Comparator;
import java.util.Date;

import pasta.domain.template.Assessment;

public class DueDateComparator implements Comparator<Assessment> {
		@Override
		public int compare(Assessment o1, Assessment o2) {
			Date now = new Date();
			
			if(o1.getDueDate().before(now) && !o2.getDueDate().before(now)) {
				return 1;
			}
			if(o2.getDueDate().before(now) && !o1.getDueDate().before(now)) {
				return -1;
			}
			
			int diff = o1.getDueDate().compareTo(o2.getDueDate());
			if(diff != 0) {
				return diff;
			}
			
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}