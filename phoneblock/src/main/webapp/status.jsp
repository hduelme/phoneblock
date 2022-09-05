<!DOCTYPE html>
<%@page import="de.haumacher.phoneblock.db.Status"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="de.haumacher.phoneblock.db.Statistics"%>
<%@page import="java.util.List"%>
<%@page import="de.haumacher.phoneblock.db.SpamReport"%>
<%@page import="de.haumacher.phoneblock.util.JspUtil"%>
<%@page import="de.haumacher.phoneblock.db.DBService"%>
<html>
<head>
<jsp:include page="head-content.jspf"></jsp:include>
</head>

<body>
<jsp:include page="header.jspf"></jsp:include>

<section class="section">
<div class="content">
	<p>
		Bekannte SPAM-Nummern: 
<%
	Status status = DBService.getInstance().getStatus();
	List<Statistics> statistic = status.getStatistics();
	int cnt = 0;
	String[] labels = {"berichtet", "bestätigt", "sicher"};
	
	for (Statistics statistics : statistic) {
		// Exclude numbers reported only once.
		if (statistics.getConfidence() > 0) {
			cnt += statistics.getCnt();
		}
		String label = labels[statistics.getConfidence()];
%>		
		<%= statistics.getCnt() %> <%= JspUtil.quote(label) %>,
<%
	}
%>	
	<%= cnt %> aktive Nummern auf der Blocklist. Insgesamt <%= status.getTotalVotes() %> User-Reports, <%= status.getArchivedReports() %> inaktive Nummer mit Spam-Verdacht.
	</p>

<%
	DateFormat format = SimpleDateFormat.getDateTimeInstance();

	long now = System.currentTimeMillis();
	List<SpamReport> reports = DBService.getInstance().getLatestSpamReports(System.currentTimeMillis() - 60 * 60 * 1000);
	if (reports.isEmpty()) {
%>
		<p>Keine aktuellen Spam-Reports.</p>
<%			
	} else {
%>
		<h2>Spam-Reports der letzten Stunde</h2> 

		<table class="table">
			<thead>
				<tr>
					<th>Phone number</th>
					<th>Confidence</th>
					<th>Received</th>
					<th>Active since</th>
				</tr>
			</thead>
			<tbody>
<%			
				for (SpamReport report : reports) {
%>
					<tr>
						<td>
							<a href="<%= request.getContextPath()%>/nums/<%= report.getPhone()%>"><%= JspUtil.quote(report.getPhone()) %></a>
						</td>
						
						<td>
							<%= report.getVotes() %>
						</td>
						
						<td>
							<%= (now - report.getLastUpdate()) / 1000 / 60 %> minutes ago
						</td>
						
						<td>
							<%= report.getDateAdded() > 0 ? format.format(new Date(report.getDateAdded())) : "-" %>
						</td>
					</tr>
<%	
				}
%>
			</tbody>
		</table>
<%	
	}
%>

<%
	reports = DBService.getInstance().getLatestBlocklistEntries();
	if (!reports.isEmpty()) {
%>
		<h2>Neuste Einträge in der Block-List</h2> 

		<table class="table">
			<thead>
				<tr>
					<th>Phone number</th>
					<th>Confidence</th>
					<th>Last update</th>
					<th>Active since</th>
				</tr>
			</thead>
			<tbody>
<%			
				for (SpamReport report : reports) {
%>
					<tr>
						<td>
							<a href="<%= request.getContextPath()%>/nums/<%= report.getPhone()%>"><%= JspUtil.quote(report.getPhone()) %></a>
						</td>
						
						<td>
							<%= report.getVotes() %>
						</td>
						
						<td>
							<%= format.format(new Date(report.getLastUpdate()))%>
						</td>

						<td>
							<%= report.getDateAdded() > 0 ? format.format(new Date(report.getDateAdded())) : "-" %>
						</td>
					</tr>
<%	
				}
%>
			</tbody>
		</table>
<%	
	}
%>

<%
	reports = DBService.getInstance().getTopSpamReports(15);
	if (!reports.isEmpty()) {
%>
		<h2>Aktuelle Top-Spammer</h2> 

		<table class="table">
			<thead>
				<tr>
					<th>Phone number</th>
					<th>Confidence</th>
					<th>Last update</th>
					<th>Active since</th>
				</tr>
			</thead>
			<tbody>
<%			
				for (SpamReport report : reports) {
%>
					<tr>
						<td>
							<a href="<%= request.getContextPath()%>/nums/<%= report.getPhone()%>"><%= JspUtil.quote(report.getPhone()) %></a>
						</td>
						
						<td>
							<%= report.getVotes() %>
						</td>
						
						<td>
							<%= format.format(new Date(report.getLastUpdate()))%>
						</td>

						<td>
							<%= report.getDateAdded() > 0 ? format.format(new Date(report.getDateAdded())) : "-" %>
						</td>
					</tr>
<%	
				}
%>
			</tbody>
		</table>
<%	
	}
%>
</div>

<div class="tile is-ancestor">
	<div class="tile is-parent is-6">
		<a class="tile is-child notification is-primary" href="<%=request.getContextPath() %>/index.jsp">
			<p class="title">Noch kein PhoneBlock?</p>
			<p class="subtitle">So wirst du den Telefonterror los!</p>
		</a>
	</div>

	<div class="tile is-parent is-6 ">
		<a class="tile is-child notification is-info" href="<%=request.getContextPath() %>/setup.jsp">
			<p class="title">Noch Fragen?</p>
			<p class="subtitle">Check die Installationsanleitung!</p>
		</a>
	</div>
</div>

</section>

<jsp:include page="footer.jspf"></jsp:include>
</body>
</html>