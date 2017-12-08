package pasta.service.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class CSVReport {

	private String[] header;
	private CSVPage[] pages;
	
	public CSVReport(String[] header, List<Object[]> results, int pageLength) {
		this.header = header;
		
		if(results.isEmpty()) {
			pages = new CSVPage[1];
			pages[0] = new CSVPage(this, 0);
			return;
		}
		
		if(pageLength <= 0) {
			pageLength = results.size();
		}
		
		int pageCount = (int) Math.ceil(results.size() / (double)pageLength);
		pages = new CSVPage[pageCount];
		
		int lineNum = -1;
		int pageNum = 0;
		
		pages[pageNum] = new CSVPage(this, pageLength);
		
		StringBuilder sb;
		for(Object[] row : results) {
			if(++lineNum >= pageLength) {
				lineNum = 0;
				pages[++pageNum] = new CSVPage(this, pageLength);
			}
			
			sb = new StringBuilder();
			
			for(int i = 0; i < row.length; i++) {
				if(i > 0) {
					sb.append(',');
				}
				if(row[i] instanceof Number) {
					sb.append(row[i].toString());
				} else {
					sb.append('"')
					  .append(row[i].toString().replace("\"", "\\\""))
					  .append('"');
				}
			}
			
			pages[pageNum].lines[lineNum] = sb.toString();
			
		}
	}

	public String[] getHeader() {
		return header;
	}
	public CSVPage[] getPages() {
		return pages;
	}

	public static class CSVPage {
		private CSVReport parent;
		private String[] lines;
		
		private CSVPage(CSVReport parent, int pageSize) {
			lines = new String[pageSize];
			this.parent = parent;
		}
		
		public void output(OutputStream out) throws IOException {
			String[] header = parent.getHeader();
			for(int i = 0; i < header.length; i++) {
				if(i > 0) {
					out.write(",".getBytes());
				}
				out.write(header[i].getBytes());
			}
			out.write(System.lineSeparator().getBytes());
			for(String line : lines) {
				if(line == null) {
					break;
				}
				out.write(line.getBytes());
				out.write(System.lineSeparator().getBytes());
			}
		}
	}
	
	public static void main(String[] args) {
		int pages = 0;
		int x = 5;
		int numDigits = (int) Math.ceil(Math.log10(pages + 1));
		System.out.println(String.format("digits: %d - %0" + numDigits + "d", numDigits, x));
	}
}