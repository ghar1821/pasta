<%@ page session="true"%>
<%@ page language="java" contentType="text/html; charset=utf-8"  pageEncoding="utf-8"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en-AU">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<meta name="Author" content="Alex Radu, Joshua Stretton" />
		<title><spring:message code="UOS" /> </title>
		
		<%--Increase the v=# number if you want to force users to re-download the CSS--%>
		<link href="<c:url value="/static/styles/screen-0.0.1-SNAPSHOT.css?v=2"/>" media="screen" rel="stylesheet" type="text/css" />
		<link href="<c:url value="/static/styles/screen-susk-0.0.1-SNAPSHOT.css?v=2"/>" media="screen" rel="stylesheet" type="text/css" />
		<link href="<c:url value="/static/styles/jquery.dataTables.min.css"/>" media="screen" rel="stylesheet" type="text/css" />
		<link href="<c:url value="/static/styles/jquery/smoothness/jquery-ui-1.8.4.custom.css"/>" media="screen" rel="stylesheet" type="text/css" />
		<link href="<c:url value="/static/scripts/jqplot/jquery.jqplot.css"/>" media="screen" rel="stylesheet" type="text/css" />
		<link href="<c:url value="/static/styles/jquery.snippet.min.css"/>" media="screen" rel="stylesheet" type="text/css" />
		<link href="<c:url value="/static/jqueryFileTree.css"/>" rel="stylesheet" type="text/css" media="screen" />
		<link href="<c:url value="/static/scripts/jwysiwyg/jquery.wysiwyg.css"/>" rel="stylesheet" type="text/css" media="screen" />
		<link href="<c:url value="/static/scripts/chosen/chosen.css"/>" rel="stylesheet" type="text/css" media="screen" />
		<link href="<c:url value="/static/styles/tipsy.css"/>" rel="stylesheet" type="text/css" media="screen" />
		
		<script type="text/javascript" src="https://www.google.com/jsapi"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery/jquery-1.8.2.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery/jquery-ui.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery/jquery-ui-timepicker-addon.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/roundTableCorners.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.snippet.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.tablesorter.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.bpopup-0.7.0.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.dataTables.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/FixedColumns.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.tristate.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jqplot/jquery.jqplot.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jwysiwyg/jquery.wysiwyg.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/chosen/chosen.jquery.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.tipsy.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.allowTab.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.search.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/tinymce/tinymce.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/tinymce/jquery.tinymce.min.js"/>"></script>
		
		<link rel="apple-touch-icon" sizes="57x57" href="<c:url value="/static/icons/apple-icon-57x57.png"/>">
		<link rel="apple-touch-icon" sizes="60x60" href="<c:url value="/static/icons/apple-icon-60x60.png"/>">
		<link rel="apple-touch-icon" sizes="72x72" href="<c:url value="/static/icons/apple-icon-72x72.png"/>">
		<link rel="apple-touch-icon" sizes="76x76" href="<c:url value="/static/icons/apple-icon-76x76.png"/>">
		<link rel="apple-touch-icon" sizes="114x114" href="<c:url value="/static/icons/apple-icon-114x114.png"/>">
		<link rel="apple-touch-icon" sizes="120x120" href="<c:url value="/static/icons/apple-icon-120x120.png"/>">
		<link rel="apple-touch-icon" sizes="144x144" href="<c:url value="/static/icons/apple-icon-144x144.png"/>">
		<link rel="apple-touch-icon" sizes="152x152" href="<c:url value="/static/icons/apple-icon-152x152.png"/>">
		<link rel="apple-touch-icon" sizes="180x180" href="<c:url value="/static/icons/apple-icon-180x180.png"/>">
		<link rel="shortcut icon" sizes="16x16" href="<c:url value="/static/icons/favicon.ico?v=3"/>"/>
		<link rel="icon" type="image/png" sizes="192x192"  href="<c:url value="/static/icons/android-icon-192x192.png"/>">
		<link rel="icon" type="image/png" sizes="32x32" href="<c:url value="/static/icons/favicon-32x32.png"/>">
		<link rel="icon" type="image/png" sizes="96x96" href="<c:url value="/static/icons/favicon-96x96.png"/>">
		<link rel="icon" type="image/png" sizes="16x16" href="<c:url value="/static/icons/favicon-16x16.png"/>">
		<link rel="manifest" href="<c:url value="/manifest.json"/>">
		<meta name="msapplication-TileColor" content="#12416c">
		<meta name="msapplication-TileImage" content="<c:url value="/static/icons/ms-icon-144x144.png"/>">
		<meta name="theme-color" content="#12416c">
		
		<decorator:head />
		
		<script type="text/javascript">
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
				
	            $(".help").each(function() {
	            	$(this).attr("helpText", $(this).html());
	            	$(this).empty();
	            	$(this).tipsy({
	            		gravity: "w",
	            		title: "helpText",
	            		html: true
	            	});
	            });
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
									<a href="<c:url value="/"/>">PASTA Home</a>
								</li>
								<li>
									<a href="http://www.sydney.edu.au">University Home</a>
								</li>
							</ul>
							<div id="quicklinks"></div>
							<div id="login">
								<c:choose>
									<c:when test="${not empty unikey}">
										<a href="<c:url value="/home/"/>"><span>${unikey.username}</span></a> |
										<a href="<c:url value="/login/exit"/>"><span>Logout</span></a>
									</c:when>
									<c:otherwise>
										<a href="<c:url value="/login/"/>"><span>Login</span></a>
									</c:otherwise>
								</c:choose>
							</div>
						</div>
					</div>
					<div id="tabbar">
						<ul class="horizontal" id="tabs">
							<li>
								<span>
									<a href="<c:url value="/home/"/>"><span>Home</span></a>
								</span>
							</li>
							<c:if test="${not empty unikey and unikey.tutor}">
								<li>
									<span>
										<a href="<c:url value="/gradeCentre/"/>"><span>Grade Centre</span></a>
									</span>
								</li>
								<c:if test="${ not empty unikey.tutorial }">
									<li>
										<span>
											<a href="<c:url value="/myTutorials/"/>"><span>My Tutorials</span></a>
										</span>
									</li>
								</c:if>
								<li>
									<span>
										<a href="<c:url value="/assessments/"/>"><span>Assessments</span></a>
									</span>
								</li>
								<li>
									<span>
										<a href="<c:url value="/unitTest/"/>"><span>Unit Tests</span></a>
									</span>
								</li>
								<li>
									<span>
										<a href="<c:url value="/handMarking/"/>"><span>Hand Marking</span></a>
									</span>
								</li>
							</c:if>
							<c:if test="${not empty unikey}">
								<li>
									<span>
										<a href="<c:url value="/competition/"/>"><span>Competitions</span></a>
									</span>
								</li>
								<li>
									<span>
										<a href="<c:url value="/admin/"/>"><span>Admin</span></a>
									</span>
								</li>
							</c:if>
						</ul>
					</div>
					<div id="tabunderscore"></div>
					<!-- start mid -->
					<div class="clearfix" id="mid"></div>
					<!-- start content -->
					<div class="nomenu nofeature pageContent">
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
