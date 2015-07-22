<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tag"%>

<%@ attribute name="rule" type="pasta.domain.form.AssessmentReleaseForm"%>
<%@ attribute name="pathPrefix" type="java.lang.String"%>


<c:set var="ruleName" value="${rule.ruleName}" />

<form:form commandName="${pathPrefix}">
	<c:if test="${not empty rule and not empty ruleName}">
		<form:hidden path="ruleId" value="${rule.ruleId}"/>
		<form:hidden path="ruleName" value="${ruleName}"/>
		<c:forEach var="ruleType" items="${allRules}">
			<c:if test="${ruleType['class'].name == ruleName}">
				<c:set var="description" value="${ruleType.description}" />
				<c:set var="shortDescription" value="${ruleType.shortDescription}" />
			</c:if>
		</c:forEach>
		<div class='float-right'>
			<a class='deleteRule'>Delete Rule</a>
		</div>
		<div class='vertical-block'>
			<div class='vertical-block'>
				<div class='horizontal-block'>
					<strong>${shortDescription}<span id="deleteMessage"></span></strong>
				</div>
				<div class='horizontal-block'>
					<a class='showChangeRule'>(change)</a>
					<div class='changeRuleDiv'>
						Change to:
						<select class='changeRule chosen'>
							<option></option>
							<c:forEach var="ruleType" items="${allRules}">
								<c:if test="${ruleType['class'].name != ruleName}">
									<option value="${ruleType['class'].name}">${ruleType.shortDescription}</option>
								</c:if>
							</c:forEach>
						</select>
					</div>
				</div>
			</div>
			<div class='vertical-block'>
				<p>${description}
			</div>
		</div>
		<div class='vertical-block'>
			<strong><c:choose><c:when test="${fn:endsWith(ruleName, 'ReleaseAndRule') or fn:endsWith(ruleName, 'ReleaseOrRule')}">Sub Rules:</c:when><c:otherwise>Options:</c:otherwise></c:choose></strong>
		</div>
		<form:errors element="div" cssClass="vertical-block" />
	</c:if>
	<div class='vertical-block'>
		<div class='vertical-block'>
			<c:choose>
			
				<c:when test="${empty rule or empty ruleName}">
					Choose a rule:
					<select class='setSubrule chosen'>
						<option></option>
						<c:forEach var="ruleType" items="${allRules}">
							<option value="${ruleType['class'].name}">${ruleType.shortDescription}</option>
						</c:forEach>
					</select>
				</c:when>
				
				<c:when test="${fn:endsWith(ruleName, 'ClassRule')}">
					<form:errors path="classes" element="div" />
					<div class='horizontal-block'>
						Classes:
						<form:select cssClass='selectAll chosen' multiple="multiple" path="classes">
							<form:options items="${allTutorials}" />
						</form:select>
					</div>
					<div class='horizontal-block'>
						<button class='chosen-toggle select'>All</button>
						<button class='chosen-toggle deselect'>Clear</button>
					</div>
				</c:when>
				
				<c:when test="${fn:endsWith(ruleName, 'DateRule')}">
					Date: <form:input path="strDate" cssClass="strDate" />
					<form:errors path="strDate" />
				</c:when>
				
				<c:when test="${fn:endsWith(ruleName, 'HasSubmittedRule')}">
					Choose assessment:
					<form:select path="compareAssessment" cssClass='chosen'>
						<option></option>
						<form:options items="${allAssessments}" itemValue="id" itemLabel="name" />
					</form:select>
					<form:errors path="compareAssessment"/>
				</c:when>		
				
				<c:when test="${fn:endsWith(ruleName, 'MarkCompareRule')}">
					<table>
						<tr>
							<td>Choose assessment:</td>
							<td>
								<form:select path="compareAssessment" cssClass='chosen'>
									<option></option>
									<form:options items="${allAssessments}" itemValue="id" itemLabel="name" />
								</form:select>
								<form:errors path="compareAssessment"/>
							</td>
						</tr>
						<tr>
							<td>Compare Mode:</td>
							<td>
								<form:select path="compareMode"><form:options itemLabel="text"/></form:select>
								<form:errors path="compareMode"/>
							</td>
						</tr>
						<tr>
							<td>Mark:</td>
							<td><form:input path="compareMark" cssClass="setMark" /> <form:checkbox path="asPercentage" label="Use percentages rather than raw marks"/></td>
						</tr>
						<tr>
							<td>Mark Type:</td>
							<td>
								<form:select path="markType"><form:options itemLabel="text"/></form:select>
								<form:errors path="markType"/>
							</td>
						</tr>
					</table>
				</c:when>
				
				<c:when test="${fn:endsWith(ruleName, 'ReleaseAndRule') or fn:endsWith(ruleName, 'ReleaseOrRule')}">
					<form:errors path="rules" element="div"/>
					<c:set var="firstOne" value="true" />
					<c:set var="conjunction" value="AND" />
					<c:if test="${fn:endsWith(ruleName, 'ReleaseOrRule')}"><c:set var="conjunction" value="OR" /></c:if>
					<c:forEach var="subrule" items="${rule.rules}" varStatus="ruleStatus">
						<c:if test="${not firstOne}"><div class='vertical-block conjunction'>${conjunction}</div></c:if>
						<div class='boxCard vertical-block ruleParent subRule' conjunction='${conjunction}' pathPrefix='${pathPrefix}.rules[${ruleStatus.index}]'>
							<tag:releaseRule rule="${subrule}" pathPrefix="${pathPrefix}.rules[${ruleStatus.index}]" />
						</div>
						<c:if test="${firstOne}"><c:set var="firstOne" value="false" /></c:if>
					</c:forEach>
					<c:if test="${empty rule.rules}">
						<div class='boxCard vertical-block ruleParent subRule' conjunction='${conjunction}' pathPrefix='${pathPrefix}.rules[0]'>
							<tag:releaseRule pathPrefix="${pathPrefix}.rules[0]" />
						</div>
					</c:if>
				</c:when>		
				
				<c:when test="${fn:endsWith(ruleName, 'StreamRule')}">
					<form:errors path="streams" element="div" />
					<div class='horizontal-block'>
						Streams:
						<form:select cssClass='selectAll chosen' multiple="multiple" path="streams">
							<form:options items="${allStreams}" />
						</form:select>
					</div>
					<div class='horizontal-block'>
						<button class='chosen-toggle select'>All</button>
						<button class='chosen-toggle deselect'>Clear</button>
					</div>
				</c:when>			
				
				<c:when test="${fn:endsWith(ruleName, 'SubmissionCountRule')}">
					<table>
						<tr>
							<td>Choose assessment:</td>
							<td>
								<form:select path="compareAssessment" cssClass='chosen'>
									<option></option>
									<form:options items="${allAssessments}" itemValue="id" itemLabel="name" />
								</form:select>
								<form:errors path="compareAssessment" />
							</td>
						</tr>
						<tr>
							<td>Compare Mode:</td>
							<td>
								<form:select path="compareMode"><form:options itemLabel="text"/></form:select>
								<form:errors path="compareMode" />
							</td>
						</tr>
						<tr>
							<td>Submission Count:</td>
							<td><form:input path="submissionCount" /></td>
						</tr>
					</table>
				</c:when>
				
				<c:when test="${fn:endsWith(ruleName, 'UsernameRule')}">
					<form:errors path="usernames" element="div" />
					<div class='horizontal-block'>
						Users:
						<form:select cssClass='selectAll chosen' multiple="multiple" path="usernames">
							<form:options items="${allUsernames}" />
						</form:select>
					</div>
					<div class='horizontal-block'>
						<button class='chosen-toggle select'>All</button>
						<button class='chosen-toggle deselect'>Clear</button>
					</div>
				</c:when>			
				
				<c:otherwise>
					Error: Invalid rule - ${ruleName}
				</c:otherwise>
			</c:choose>
		</div>
	</div>
</form:form>