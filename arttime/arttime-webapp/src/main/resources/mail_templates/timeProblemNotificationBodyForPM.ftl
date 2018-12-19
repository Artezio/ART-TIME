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
			<td>- Period: ${period.start?date} - ${period.finish?date}</td>
		</tr>
		<tr>
			<td>- Comment: <#if comment??><b>${comment}</b></#if></td>
		</tr>
		<#if appHost??>
			<tr>
				<td>Details: 
					<a href="${appHost}/pages/manageEfforts/manageEfforts.xhtml?start=${period.start?date}&finish=${period.finish?date}&usernames=${userNames}">
						Arttime manage efforts
					</a>
				</td>
			</tr>	
		</#if>
		
		
		<#list employeeWorkTimeProblems as problem>
		<tr>
			<hr />
		</tr>
		<tr>
			<table>
				<tbody>
					<tr>
						<td>${problem.employee.lastName} ${problem.employee.firstName}</td>
					</tr>
					<tr>
						<td>- Work time deviation: ${problem.deviation} hours.</td>
					</tr>
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
									<#list problem.deviationDetails as entry>
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
		</tr>
		</#list>

	</tbody>
</table>

<p>Time Management System</p>