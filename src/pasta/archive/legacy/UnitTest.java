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

package pasta.archive.legacy;

import pasta.util.ProjectProperties;
/**
 * Container class for a unit test.
 * 
 * Contains the name of the unit test and whether it has been tested.
 * 
 * String representation:
 * <pre>{@code <unitTestProperties>
	<name>name</name>
	<tested>true|false</tested>
</unitTestProperties>}</pre>
 * 
 * <p>
 * File location on disk: $projectLocation$/template/unitTest/$unitTestName$
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 *
 */
public class UnitTest {
	private String name;
	private boolean tested;
	
	/**
	 * Default constructor
	 * <p>
	 * name="nullgarbagetemptestihopenobodynamestheirtestthis"
	 * 
	 * tested=false
	 * 
	 */
	public UnitTest(){
		this.name = "nullgarbagetemptestihopenobodynamestheirtestthis";
		this.tested = false;
	}
	
	public UnitTest(String name, boolean tested){
		this.name = name;
		this.tested = tested;
	}

	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return name.replace(" ", "");
	}

	public String getFileLocation(){
		return ProjectProperties.getInstance().getProjectLocation()+"/template/unitTest/"+getShortName();
	}
	
	public boolean isTested() {
		return tested;
	}
	
	public void setTested(boolean tested) {
		this.tested = tested;
	}
	
	@Override
	public String toString(){
		String output = "<unitTestProperties>" + System.getProperty("line.separator");
		output += "\t<name>"+name+"</name>" + System.getProperty("line.separator");
		output += "\t<tested>"+tested+"</tested>" + System.getProperty("line.separator");
		output += "</unitTestProperties>";
		return output;
	}
}