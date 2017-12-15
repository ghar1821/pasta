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

package pasta.domain;
/**
 * Different permission levels:
 * <p>
 * STUDENT
 * <ul>
 * 	<li>Allowed to submit assessment.</li>
 * 	<li>Must abide by any restrictions (e.g. time, number of submissions) set by the instructor.</li>
 * 	<li>Can view feedback details.</li>
 * </ul>
 * TUTOR
 * <ul>
 * 	<li>Can view all assessments or assessment components but cannot modify</li>
 * 	<li>Can submit to any assessment</li>
 * 	<li>Can submit for a student</li>
 * 	<li>Can bypass any restrictions set by the instructor</li>
 * 	<li>Can change their own tutorial classes.</li>
 * 	<li>Can change the tutorial allocation for students.</li>
 * 	<li>Can add/delete students from the system (adding/removing is loss-less in terms of assessment submissions)</li>
 * </ul>
 *  INSTRUCTOR
 * <ul>
 * 	<li>Can view and modify all assessments or assessment components</li>
 * 	<li>Can submit to any assessment</li>
 * 	<li>Can submit for a student</li>
 * 	<li>Can bypass any restrictions set by the instructor</li>
 * 	<li>Can change their own tutorial classes.</li>
 * 	<li>Can change the tutorial allocation for students.</li>
 * 	<li>Can add/delete students from the system (adding/removing is loss-less in terms of assessment submissions)</li>
 * </ul>
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-12-10
 */
public enum UserPermissionLevel {
	STUDENT("Student"), TUTOR("Tutor"), INSTRUCTOR("Instructor"), GROUP("Group");
	
	private UserPermissionLevel(String desc) {
		this.description = desc;
	}
	private String description;
	public String getDescription() {
		return description;
	}
	@Override
	public String toString() {
		return getDescription();
	}
	public static UserPermissionLevel[] validReportValues() {
		return new UserPermissionLevel[] {STUDENT, TUTOR, INSTRUCTOR};
	}
}
