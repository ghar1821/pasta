package pasta.view;


import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import pasta.domain.Assessment2;
import pasta.domain.User;

/**
 * Class to generate an excel document of everyone's marks that can be downloaded by a tutor.
 * @author Alex
 *
 */
public class ExcelMarkView extends AbstractExcelView{

	@Override
	protected void buildExcelDocument(Map<String, Object> map, HSSFWorkbook workbook,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		// get data
		Map<User, Map<String, Assessment2>> studentList = (Map<User, Map<String, Assessment2>>) map.get("allStudents");
		List<String> assList = (List<String>) map.get("allAssessments");
		
		// create the worksheet
		HSSFSheet sheet = workbook.createSheet("Marks");
		// create the header
		HSSFRow header = sheet.createRow(0);
		
		// add header data
		header.createCell(0).setCellValue("Unikey");
		for(int i=0; i<assList.size(); ++i){
			header.createCell(i+1).setCellValue(assList.get(i));
		}
		header.createCell(assList.size()+1).setCellValue("Total");
		
		// add the values.
		int rowNum=1;
		for(Entry<User, Map<String, Assessment2>> student: studentList.entrySet()){
			if(!student.getKey().isTutor()){
				HSSFRow currRow = sheet.createRow(rowNum);
				currRow.createCell(0).setCellValue(student.getKey().getUnikey());
				double total = 0;
				for(int j=0; j<assList.size(); ++j){
					if(student.getValue().get(assList.get(j)) == null){
						currRow.createCell(j+1).setCellValue("N/A");
					}
					else{
						currRow.createCell(j+1).setCellValue(student.getValue().get(assList.get(j)).getPercentage());
						total += student.getValue().get(assList.get(j)).getWeightedMark();
					}
				}
				currRow.createCell(assList.size()+1).setCellValue(total);
				++rowNum;
			}
		}
	}

}
