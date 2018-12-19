<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<#setting number_format="#.00">
<#setting date_format="dd.MM.yy">
Work time problems:
<table>
	<tbody>
		<tr>
			<td>${recipient.lastName} ${recipient.firstName}</td>
		</tr>
		<tr>
			<td>- Problem hour type: ${hourType.type}.</td>
		</tr>
		<tr>
			<td>- Work time deviation: ${deviation} hours.</td>
		</tr>
		<tr>
			<td>- Period: ${period.start?date} - ${period.finish?date}</td>
		</tr>
		<tr>
			<td>- Comment: <#if comment??><b>${comment}</b></#if></td>
		</tr>
		<#if appHost??>
			<tr>
					<td>Details: 
						<a href="${appHost}/pages/timesheet/timesheet.xhtml?start=${period.start?date}&finish=${period.finish?date}">
						Arttime timesheet
					</a>
				</td>
			</tr>
		</#if>
		<tr>
			<td><table border="1">
					<thead>
						<tr>
							<th scope="col">Date</th>
							<th scope="col">Deviation</th>
							<th scope="col">State</th>
						</tr>
					</thead>
					<tbody>
						<#list deviationDetails as entry>
						<tr>
							<td>${entry.key?date}</td>
							<td>
								<div align="right" width="100%">${entry.value}</div>
							</td>
							<td><#if entry.value < 0 >unapproved hours<#else>overtime</#if></td>
						</tr>
						</#list>
					</tbody>
				</table></td>
		</tr>
	</tbody>
</table>

<p>Time Management System</p>