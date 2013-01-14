<%@ page session="true"%>
<%@ page language="java" contentType="text/html; charset=utf-8"  pageEncoding="utf-8"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta http-equiv="Content-Script-Type" content="text/javascript" />
		<meta http-equiv="Content-Style-Type" content="text/css" />
		<meta http-equiv="Content-Language" content="en" />
		<meta name="Author" content="Alex Radu" />
		<title><spring:message code="UOS" /> </title>
		
		<link href="<spring:url value="/static/styles/screen-0.0.1-SNAPSHOT.css" htmlEscape="true" />" media="screen" rel="stylesheet" type="text/css" />
		<link href="<spring:url value="/static/styles/screen-susk-0.0.1-SNAPSHOT.css" htmlEscape="true" />" media="screen" rel="stylesheet" type="text/css" />
		<link href="<spring:url value="/static/styles/jquery/smoothness/jquery-ui-1.8.4.custom.css" htmlEscape="true" />" media="screen" rel="stylesheet" type="text/css" />
		<link href="<spring:url value="/static/images/favicon.ico" htmlEscape="true" />" rel="shortcut icon" />
		<link href="<spring:url value="/static/scripts/jqplot/jquery.jqplot.css" htmlEscape="true" />" media="screen" rel="stylesheet" type="text/css" />
		<link href="<spring:url value="/static/styles/jquery.snippet.min.css" htmlEscape="true" />" media="screen" rel="stylesheet" type="text/css" />
		<link href="<spring:url value="/static/jqueryFileTree.css" htmlEscape="true" />" rel="stylesheet" type="text/css" media="screen" />
		<link href="<spring:url value="/static/styles/jquery.dataTables.css" htmlEscape="true" />" media="screen" rel="stylesheet" type="text/css" />
		
		<script type="text/javascript" src="<spring:url value="/static/scripts/jquery/jquery-1.8.2.js" htmlEscape="true" />"></script>
		<script type="text/javascript" src="<spring:url value="/static/scripts/jquery/jquery-ui.js" htmlEscape="true" />"></script>
		<script type="text/javascript" src="<spring:url value="/static/scripts/jquery/jquery-ui-timepicker-addon.js" htmlEscape="true" />"></script>
		<script type="text/javascript" src="<spring:url value="/static/scripts/roundTableCorners.js" htmlEscape="true" />"></script>
		<script type="text/javascript" src="<spring:url value="/static/scripts/jquery.snippet.min.js" htmlEscape="true" />"></script>
		<script type="text/javascript" src="<spring:url value="/static/scripts/jquery.tablesorter.js" htmlEscape="true" />"></script>
		<script type="text/javascript" src="<spring:url value="/static/scripts/jquery.bpopup-0.7.0.min.js" htmlEscape="true" />"></script>
		<script type="text/javascript" src="<spring:url value="/static/scripts/jquery.dragtable.js" htmlEscape="true" />"></script>
		<script type="text/javascript" src="<spring:url value="/static/scripts/jquery.dataTables.js" htmlEscape="true" />"></script>
		<script type="text/javascript" src="<spring:url value="/static/scripts/FixedColumns.js" htmlEscape="true" />"></script>
		
		<decorator:head />
		
		<script>
			$(document).ready(function(){
				$("pre.ccode").snippet("c",{style:"ide-eclipse",transparent:false,numbered:true});
				$("pre.cppcode").snippet("cpp",{style:"ide-eclipse",transparent:false,numbered:true});
				$("pre.csharpcode").snippet("csharp",{style:"ide-eclipse",transparent:false,numbered:true});
				$("pre.csscode").snippet("css",{style:"ide-eclipse",transparent:false,numbered:true});
				$("pre.flexcode").snippet("flex",{style:"ide-eclipse",transparent:false,numbered:true});
				$("pre.htmlcode").snippet("html",{style:"ide-eclipse",transparent:false,numbered:true});
				$("pre.javacode").snippet("java",{style:"ide-eclipse",transparent:false,numbered:true});
				$("pre.javascriptcode").snippet("javascript",{style:"ide-eclipse",transparent:false,numbered:true});
				$("pre.javascriptdomcode").snippet("javascript_dom",{style:"ide-eclipse",transparent:false,numbered:true});
				$("pre.perlcode").snippet("perl",{style:"ide-eclipse",transparent:false,numbered:true});
				$("pre.phpcode").snippet("php",{style:"ide-eclipse",transparent:false,numbered:true});
				$("pre.pythoncode").snippet("python",{style:"ide-eclipse",transparent:false,numbered:true});
				$("pre.rubycode").snippet("ruby",{style:"ide-eclipse",transparent:false,numbered:true});
				$("pre.sqlcode").snippet("sql",{style:"ide-eclipse",transparent:false,numbered:true});
				$("pre.xmlcode").snippet("xml",{style:"ide-eclipse",transparent:false,numbered:true});
			});
		</script>
	</head>
	<body id="home">
		<div id="w1">
			<div id="w2">
				<div id="w3">
					<div id="head">
						<div id="masthead">
							<h1>
								<a href="http://www.sydney.edu.au" id="logo">The University of Sydney</a> <span id="separator">-</span> <span id="tag-line"><spring:message code="UOS" /></span>
							</h1>
						</div>
						<div id="utilities">
							<ul id="nav-global">
								<li class="active">
									<a href="<spring:url value="/" htmlEscape="true" />">BL Home</a>
								</li>
								<li>
									<a href="http://www.sydney.edu.au">University Home</a>
								</li>
							</ul>
							<div id="quicklinks"></div>
							<div id="login">
								<c:choose>
									<c:when test="${not empty unikey}">
										<a href="<spring:url value="/home/" htmlEscape="true" />"><span>${unikey.username}</span></a> |
										<a href="<spring:url value="/login/exit" htmlEscape="true" />"><span>Logout</span></a>
									</c:when>
									<c:otherwise>
										<a href="<spring:url value="/login/" htmlEscape="true" />"><span>Login</span></a>
									</c:otherwise>
								</c:choose>
							</div>
						</div>
					</div>
					<div id="tabbar">
						<ul class="horizontal" id="tabs">
							<li>
								<span>
									<a href="<spring:url value="/home/" htmlEscape="true" />"><span>Home</span></a>
								</span>
							</li>
							<c:if test="${not empty unikey and unikey.tutor}">
								<li>
									<span>
										<a href="<spring:url value="/gradeCentre/" htmlEscape="true" />"><span>Grade Centre</span></a>
									</span>
								</li>
								<li>
									<span>
										<a href="<spring:url value="/assessments/" htmlEscape="true" />"><span>Assessments</span></a>
									</span>
								</li>
								<li>
									<span>
										<a href="<spring:url value="/unitTest/" htmlEscape="true" />"><span>Unit Tests</span></a>
									</span>
								</li>
								<li>
									<span>
										<a href="<spring:url value="/handMarking/" htmlEscape="true" />"><span>Hand Marking</span></a>
									</span>
								</li>
							</c:if>
						</ul>
					</div>
					<div id="tabunderscore"></div>
					<!-- start mid -->
					<div class="clearfix" id="mid" />
					<!-- start content -->
					<div class="nomenu nofeature" id="content">
						<div id="w4">
							<decorator:body />
						</div>
					</div>
				</div>
			</div>
		</div>
		<div id="lbOverlay" style="display: none;"></div>
		<div id="lbCenter" style="display: none;">
			<div id="lbImage">
				<div style="position: relative;">
					<a id="lbPrevLink" href="#"></a>
					<a id="lbNextLink" href="#"></a>
				</div>
			</div>
		</div>
		<div id="lbBottomContainer" style="display: none;">
			<div id="lbBottom">
				<a id="lbCloseLink" href="#"></a>
				<div id="lbCaption"></div>
				<div id="lbNumber"></div>
				<div style="clear: both;"></div>
			</div>
		</div>
	</body>
</html>