/*
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
 */

package pasta.domain.template;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import pasta.util.ProjectProperties;

/**
 * Container class for a unit test. Contains the name of the unit test and
 * whether it has been tested. String representation:
 * 
 * <pre>
 * {@code <unitTestProperties>
 * 	<name>name</name>
 * 	<tested>true|false</tested>
 * </unitTestProperties>}
 * </pre>
 * <p>
 * File location on disk: $projectLocation$/template/unitTest/$unitTestId$
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 */

@Entity
@Table (name = "unit_tests")
public class UnitTest implements Serializable, Comparable<UnitTest> {
	
	private static final long serialVersionUID = -7413957282304051135L;

	@Id @GeneratedValue
	private long id;
	
	private String name;
	
	private boolean tested;

	/**
	 * Default constructor
	 * <p>
	 * name="nullgarbagetemptestihopenobodynamestheirtestthis" tested=false
	 */
	public UnitTest() {
		this.name = "nullgarbagetemptestihopenobodynamestheirtestthis";
		this.tested = false;
	}

	public UnitTest(String name, boolean tested) {
		this.name = name;
		this.tested = tested;
	}

	public String getName() {
		return name;
	}

	public String getFileAppropriateName() {
		return name.replace("[^\\w]+", "");
	}

	public String getFileLocation() {
		return ProjectProperties.getInstance().getUnitTestsLocation() + getId();
	}

	public boolean isTested() {
		return tested;
	}

	public void setTested(boolean tested) {
		this.tested = tested;
	}
		
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public String toString(){
		String output = "<unitTestProperties>" + System.getProperty("line.separator");
		output += "\t<id>"+id+"</id>" + System.lineSeparator();
		output += "\t<name>" + name + "</name>" + System.getProperty("line.separator");
		output += "\t<tested>" + tested + "</tested>" + System.getProperty("line.separator");
		output += "</unitTestProperties>";
		return output;
	}

	@Override
	public int compareTo(UnitTest other) {
		return this.name.compareTo(other.name);
	}
}
