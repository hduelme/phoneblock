<!DOCTYPE html>
<%@page import="de.haumacher.phoneblock.app.LoginFilter"%>
<%@page import="de.haumacher.phoneblock.app.LoginServlet"%>
<%@page import="de.haumacher.phoneblock.app.SettingsServlet"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8" session="false"%>
<html>
<%
	request.setAttribute("title", "PhoneBlock unterstützen");
%>
<head>
<jsp:include page="head-content.jspf"></jsp:include>
</head>

<body>
<jsp:include page="header.jspf"></jsp:include>

<section class="section">
	<div class="content">
		<h1>Per Banküberweisung spenden</h1>

		<p>
			Vielen Dank, dass Du Dich an den Kosten von PhoneBlock beteiligst. Wenn viele mitmachen, reicht ein kleiner Betrag reicht aus, z.B. 
			<b>1€ pro Jahr</b> oder <b>0,05€ pro abgefangenem Spam-Anruf</b>, Du kannst die höhe Deines Beitrages aber selbst wählen.
		</p>
		
		<p>
			Bitte nutze die folgenden Kontodaten für Deine Überweisung:
		</p>

		<ul>
			<li>Empfänger: <code id="receiver">${bank.receiver}</code> <a title="In die Zwischenablage kopieren." href="#" onclick="return copyToClipboard('receiver');"><i class="fa-solid fa-copy"></i></a></li>
			<li>Kontonummer: <code id="account">${bank.account}</code> <a title="In die Zwischenablage kopieren." href="#" onclick="return copyToClipboard('account');"><i class="fa-solid fa-copy"></i></a></li>
			<li>BIC: <code id="bic">${bank.bic}</code> <a title="In die Zwischenablage kopieren." href="#" onclick="return copyToClipboard('bic');"><i class="fa-solid fa-copy"></i></a></li>
			<li>Verwendungszweck: <code id="purpose">PhoneBlock-<%= LoginFilter.getAuthenticatedUser(request.getSession())%></code> <a title="In die Zwischenablage kopieren." href="#" onclick="return copyToClipboard('purpose');"><i class="fa-solid fa-copy"></i></a></li>
		</ul>
		
		<p>
			Vielen Dank, dass Du einen Beitrag für die Nutzung von PhoneBlock leistest! 
		</p>
	</div>
</section>

<jsp:include page="footer.jspf"></jsp:include>
</body>
</html>
