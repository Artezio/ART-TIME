<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<#setting number_format="#.00">
<#setting date_format="dd.MM.yy">

<p>The reported time '${hourType.type}' doesn't correspond to the expected time for the period ${period.start?date} - ${period.finish?date}.</p>
<p>Please fill in the time correctly in <a href="${appHost}/pages/timesheet/timesheet.xhtml?start=${period.start?date}&finish=${period.finish?date}">the timesheet</a>.</p>

<p>Time Management System</p>