<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h1>Downloads</h1>

<div class='section'>
	<h2 class='section-title'>Unit Test Results History</h2>
	<div class='part no-line'>
		Download a CSV file (or multiple CSV files) containing the unit testing results from every submission.
	</div>
	<form action="utchistory/" method="post">
		<div class='part'>
			<div class='pasta-form'>
				<div class='pf-item'>
					<div class='pf-label'>Maximum rows per CSV file:</div>
					<div class='pf-input'>
						<input class="maxRowCount" name="maxRowCount" type="number" min="0" value="0" />
					</div>
				</div>
				<div class='pf-item'>
					<p>Set max rows to:
					<ul>
						<li><a href="javascript:setRows(0)">0</a> for all data on one CSV document
						<li><a href="javascript:setRows(65536)">65,536</a> for CSV files to be imported to MS Excel 2003 and earlier.
						<li><a href="javascript:setRows(1048576)">1,048,576</a> for CSV files to be imported to MS Excel 2007 and later.
					</ul>
				</div>
				<div class='button-panel'>
					<button type='submit'>Download</button>
				</div>
			</div>
		</div>
	</form>
</div>

<div class='section'>
	<h2 class='section-title'>Submissions History</h2>
	<div class='part no-line'>
		Download a CSV file (or multiple CSV files) containing details of every submission, including user(s) involved, submission date and automatic mark percentage.
	</div>
	<div class='part no-line'>
		<strong>Note:</strong> the "assessment_release_date" column only applies to assessments that have a single date release rule. Any assessment with a more complicated release rule (or none at all) will have a blank entry for this column.
	</div>
	<form action="submissionhistory/" method="post">
		<div class='part'>
			<div class='pasta-form'>
				<div class='pf-item'>
					<div class='pf-label'>Maximum rows per CSV file:</div>
					<div class='pf-input'>
						<input class="maxRowCount" name="maxRowCount" type="number" min="0" value="0" />
					</div>
				</div>
				<div class='pf-item'>
					<p>Set max rows to:
					<ul>
						<li><a href="javascript:setRows(0)">0</a> for all data on one CSV document
						<li><a href="javascript:setRows(65536)">65,536</a> for CSV files to be imported to MS Excel 2003 and earlier.
						<li><a href="javascript:setRows(1048576)">1,048,576</a> for CSV files to be imported to MS Excel 2007 and later.
					</ul>
				</div>
				<div class='button-panel'>
					<button type='submit'>Download</button>
				</div>
			</div>
		</div>
	</form>
</div>

<div class='section'>
	<h2 class='section-title'>Database Download</h2>
	<div class='part no-line'>
		Download an SQL dump of the contents of the PASTA database for analysis.
	</div>
	<div class='button-panel'>
		<form action="dbdump/" method="post">
			<button type='submit'>Download</button>
		</form>
	</div>
</div>

<script>
	function setRows(num) {
		$(this).closest(".pasta-form").find(".maxRowCount").val(num);
	}
</script>