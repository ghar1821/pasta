package pasta.view;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import pasta.domain.PASTAUser;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;

/**
 * Class to generate an excel document of everyone's marks that can be downloaded by a tutor.
 * @author Alex
 *
 */
public class ExcelAutoMarkView extends AbstractExcelView{

	@Override
	protected void buildExcelDocument(Map<String, Object> map, HSSFWorkbook workbook,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		// get data
		Map<String, Map<String, AssessmentResult>> resultList = (Map<String, Map<String, AssessmentResult>>) map.get("latestResults");
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
					if(resultList.get(student.getUsername()) == null || resultList.get(student.getUsername()).get(assList.get(j).getShortName()) == null){
						currRow.createCell(j+3).setCellValue("N/A");
					}
					else{
						double mark = resultList.get(student.getUsername()).get(assList.get(j).getShortName()).getAutoMarks();
						currRow.createCell(j+3).setCellValue(mark);
						total += mark;
					}
				}
				currRow.createCell(assList.size()+3).setCellValue(total);
				++rowNum;
			}
		}
	}

}
