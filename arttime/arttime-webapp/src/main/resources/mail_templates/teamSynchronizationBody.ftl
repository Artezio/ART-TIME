<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
${managedProject.code} team has been updated after synchronization with LDAP
<p>
	<#if newEmployees?has_content>
		List of new employees at your project:
		<table border="1">
			<thead>
				<tr>
					<th scope="col">Employee</th>							
				</tr>
			</thead>
			<tbody>
				<#list newEmployees as employee>
					<tr>
						<td>${employee.fullName}</td>
					</tr>
				</#list>
			</tbody>
		</table>
	</#if>
	<#if closedEmployees?has_content>
		Following employees have been removed from team:
		<table border="1">
			<thead>
				<tr>
					<th scope="col">Employee</th>
				</tr>
			</thead>
			<tbody>
				<#list closedEmployees as employee>
					<tr>
						<td>${employee.fullName}</td>
					</tr>
				</#list>
			</tbody>
		</table>
	</#if>
</p>
<p>
	<i>If you wish to correct project team information, please follow the link ${appUrl}/pages/project/editProject.xhtml?project=${managedProject.id}</i>
</p>
<p>Time Management System</p>