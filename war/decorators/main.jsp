<%@ page session="true"%>
<%@ page language="java" contentType="text/html; charset=utf-8"  pageEncoding="utf-8"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en-AU">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<meta name="Author" content="Alex Radu, Joshua Stretton, Vincent Gramoli" />
		<title><spring:message code="UOS" /> </title>
		
		<%--Increase the v=# number if you want to force users to re-download the CSS--%>
		<link href="<c:url value="/static/styles/main.css?v=2"/>" media="screen" rel="stylesheet" type="text/css" />
		<link href="<c:url value="/static/styles/theme.css?v=2"/>" media="screen" rel="stylesheet" type="text/css" />
		<link href="<c:url value="/static/styles/loading.css"/>" media="screen" rel="stylesheet" type="text/css" />
		<link href="<c:url value="/static/styles/jquery.dataTables.min.css"/>" media="screen" rel="stylesheet" type="text/css" />
		<link href="<c:url value="/static/styles/jquery/smoothness/jquery-ui-1.8.4.custom.css"/>" media="screen" rel="stylesheet" type="text/css" />
		<link href="<c:url value="/static/styles/jquery.snippet.min.css"/>" media="screen" rel="stylesheet" type="text/css" />
		<link href="<c:url value="/static/jqueryFileTree.css"/>" rel="stylesheet" type="text/css" media="screen" />
		<link href="<c:url value="/static/scripts/chosen/chosen.css"/>" rel="stylesheet" type="text/css" media="screen" />
		<link href="<c:url value="/static/styles/tipsy.css"/>" rel="stylesheet" type="text/css" media="screen" />
		<link href="<c:url value="/static/styles/jquery.collapsible.css"/>" rel="stylesheet" type="text/css" media="screen" />
		<link href="<c:url value="/static/styles/font-awesome.min.css"/>" rel="stylesheet" type="text/css" media="screen" />
		
		<script type="text/javascript" src="https://www.google.com/jsapi"></script>
		<script type="text/javascript" src="https://use.fontawesome.com/d4d3b7da4d.js"></script>
		<script type="text/javascript" src="https://code.highcharts.com/4.2.2/highcharts.js"></script>
		<script type="text/javascript" src="https://code.highcharts.com/4.2.2/modules/exporting.js"></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.18.1/moment.min.js"></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.18.1/locale/en-au.js"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery/jquery-1.8.2.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery/jquery-ui.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery/jquery-ui-timepicker-addon.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.snippet.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.bpopup-0.11.0.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.dataTables.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/chosen/chosen.jquery.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.tipsy.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.allowTab.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.search.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/tinymce/tinymce.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/tinymce/jquery.tinymce.min.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.collapsible.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.colours.js"/>"></script>
<<<<<<< Upstream, based on origin/master
		<script type="text/javascript" src="<c:url value="/static/scripts/jquery.loading.js"/>"></script>
=======
>>>>>>> 25b2760 Updated colours of collapsible elements.
		
		<script type="text/x-mathjax-config">
			MathJax.Hub.Config({
				tex2jax: {
					inlineMath: [ ['$$','$$'], ['\\(','\\)'] ],
					displayMath: [ ['$$$','$$$'], ['\[','\]'] ],
					processClass: "show-math"
				}
			});
		</script>
		<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-AMS_HTML"></script>
		
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
	            	$(this).addClass("loaded");
	            });
	            
	            var specificTitle = $('#body h1:first').text();
	            if(specificTitle) {
		            document.title = specificTitle;
	            }
	            
				});
		</script>
	</head>
	<body id="home" class="tex2jax_ignore">
		<div id='header'>
			<div id='head'>
				<img src="<c:url value="/static/images/usyd-100.svg"/>">
				<div class='horizontal float-right'><span class="title"><spring:message code="UOS" /></span></div>
			</div>
			
			<div id="login" class='link-bar float-right'>
				<c:choose>
					<c:when test="${not empty user}">
						<a href="<c:url value="/home/"/>"><span>${user.username}</span></a>
						<a href="<c:url value="/login/exit"/>"><span>Logout</span></a>
					</c:when>
					<c:otherwise>
						<a href="<c:url value="/login/"/>"><span>Login</span></a>
					</c:otherwise>
				</c:choose>
			</div>
			
			<div class='link-bar'>
				<a href="<c:url value="/"/>">PASTA Home</a>
				<a href="http://www.sydney.edu.au">University Home</a>
			</div>
			
			<div class='tab-bar'>
				<div class='tab'><a href="<c:url value="/home/"/>">Home</a></div>
				<c:if test="${not empty user and user.tutor}">
					<div class='tab'><a href="<c:url value="/gradeCentre/"/>">Grade Centre</a></div>
					<c:if test="${ not empty user.tutorial }">
						<div class='tab'><a href="<c:url value="/myTutorials/"/>">My Tutorials</a></div>
					</c:if>
					<div class='tab'><a href="<c:url value="/assessments/"/>">Assessments</a></div>
					<div class='tab'><a href="<c:url value="/unitTest/"/>">Unit Tests</a></div>
					<div class='tab'><a href="<c:url value="/handMarking/"/>">Hand Marking</a></div>
				</c:if>
				<c:if test="${not empty user}">
					<div class='tab'><a href="<c:url value="/competition/"/>">Competitions</a></div>
					<c:if test="${empty viewedUser}">
						<div class='tab'><a href="<c:url value="/reporting/"/>">Reporting</a></div>
					</c:if>
					<c:if test="${not empty viewedUser}">
						<div class='tab'><a href="<c:url value="/reporting/user/${viewedUser.username}/"/>">Reporting</a></div>
					</c:if>
					<div class='tab'><a href="<c:url value="/admin/"/>">Admin</a></div>
				</c:if>
			</div>
		</div>
		
		<script>
			$("#header .tab-bar .tab a").filter(function(){return window.location.href.indexOf(this.href) === 0;}).parent().addClass("current");
		
			var c1 = $("#header .tab-bar").css("background-color");
			$.fn.collapsible.defaults.style.hover.background = c1;
			$.fn.collapsible.defaults.style.color = c1;
			var c2 = Colours.brighter(c1);
			$.fn.collapsible.defaults.style.hover.color = c2;
			$.fn.collapsible.defaults.style.background = c2;
		</script>
		
		<div id="body">
			<decorator:body />
		</div>
	</body>
</html>
