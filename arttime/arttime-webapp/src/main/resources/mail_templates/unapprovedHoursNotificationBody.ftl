<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<#setting number_format="#.00">
<#setting date_format="dd.MM.yy">

<p>Your managed projects contain unapproved hours for the period ${period.start?date} - ${period.finish?date}.</p>
<p>Please approve the timesheet on <a href="${appHost}/pages/manageEfforts/manageEfforts.xhtml?start=${period.start?date}&finish=${period.finish?date}">the manage efforts page</a>, if necessary.</p>

<p>Time Management System</p>