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

package pasta.domain.moss;

/**
 * Class to hold the plagiarism measure between two students.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-01-31
 *
 */
public class MossPairings {
	private String student1;
	private String student2;
	private int percentage1;
	private int percentage2;
	private int lines;
	
	public MossPairings(String student1, String student2,
			int percentage1, int percentage2, int lines){
		this.student1 = student1;
		this.student2 = student2;
		this.percentage1 = percentage1;
		this.percentage2 = percentage2;
		this.lines = lines;
	}
	
	public String getStudent1(){
		return student1;
	}
	
	public String getStudent2(){
		return student2;
	}
	
	public int getPercentage1(){
		return percentage1;
	}
	
	public int getPercentage2(){
		return percentage2;
	}
	
	public int getLines(){
		return lines;
	}
	
	public int getMaxPercentage(){
		return Math.max(percentage1, percentage2);
	}
}
