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

import pasta.domain.result.CombinedAssessmentResult;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;

/**
 * Class to generate an excel document of everyone's automatic marks that can be downloaded by a tutor.
 * <p>
 * The marks that come from only automatic marking (e.g. unit tests, competitions).
 * This does not use any of the hand marking templates.
 * 
 * This class is almost a carbon copy of {@link pasta.view.ExcelMarkView}.
 * 
 * I was kinda lazy when Michael wanted this class, so copy-paste to the rescue.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-03-21
 *
 */
public class ExcelAutoMarkView extends AbstractExcelView{

	@Override
	protected void buildExcelDocument(Map<String, Object> map, HSSFWorkbook workbook,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		// get data
		Map<PASTAUser, Map<Long, CombinedAssessmentResult>> resultList = (Map<PASTAUser, Map<Long, CombinedAssessmentResult>>) map.get("latestResults");
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
						double mark = resultList.get(student).get(assList.get(j).getId()).getAutoMarks();
						currRow.createCell(j+3).setCellValue(mark);
						total += mark;
					}
				}
				currRow.createCell(assList.size()+3).setCellValue(total);
				++rowNum;
			}
		}
		
		response.setHeader( "Content-Disposition", "filename=\"pasta-auto-marks.xls\"" );
	}

}
