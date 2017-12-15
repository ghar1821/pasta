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

package pasta.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;

/**
 * Class to generate an excel document of everyone's marks that can be downloaded by a tutor.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 *
 */
public class ExcelMarkView extends AbstractExcelView{

	@Override
	protected void buildExcelDocument(Map<String, Object> map, HSSFWorkbook workbook,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		// get data
		Map<PASTAUser, Map<Long, AssessmentResult>> resultList = (Map<PASTAUser, Map<Long, AssessmentResult>>) map.get("latestResults");
		ArrayList<Assessment> assList = new ArrayList<Assessment>((Collection<Assessment>) map.get("assessmentList"));
		Collection<PASTAUser> userList = (Collection<PASTAUser>)map.get("userList");
		
		// create the worksheet
		HSSFSheet sheet = workbook.createSheet("Marks");
		// create the header
		HSSFRow header = sheet.createRow(0);
		
		// add header data
		header.createCell(0).setCellValue("Username");
		header.createCell(1).setCellValue("Stream");
		header.createCell(2).setCellValue("Class");

		for(int i=0; i<assList.size(); ++i){
			header.createCell(i+3).setCellValue(assList.get(i).getName());
		}
		header.createCell(assList.size()+3).setCellValue("Total");
		
		// add the values.
		int rowNum=1;
		for(PASTAUser student: userList){
			if(!student.isTutor()){
				HSSFRow currRow = sheet.createRow(rowNum);
				currRow.createCell(0).setCellValue(student.getUsername());
				currRow.createCell(1).setCellValue(student.getStream());
				currRow.createCell(2).setCellValue(student.getTutorial());
				double total = 0;
				for(int j=0; j<assList.size(); ++j){
					if(resultList.get(student) == null || resultList.get(student).get(assList.get(j).getId()) == null){
						currRow.createCell(j+3).setCellValue("N/A");
					}
					else{
						double mark = resultList.get(student).get(assList.get(j).getId()).getMarks();
						currRow.createCell(j+3).setCellValue(mark);
						total += mark;
					}
				}
				currRow.createCell(assList.size()+3).setCellValue(total);
				++rowNum;
			}
		}
		
		response.setHeader( "Content-Disposition", "filename=\"pasta-marks.xls\"" );
	}

}
