<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h1>Downloads</h1>

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