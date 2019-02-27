<!doctype html>

<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="robots" content="noindex, nofollow">
		<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1.0, shrink-to-fit=no">
		<meta http-equiv="Cache-control" content="no-cache" />
		<meta http-equiv="Expires" content="-1" />
		<meta http-equiv="Pragma" content="no-cache" />
		
		
        <#if properties.meta?has_content>
            <#list properties.meta?split(' ') as meta>
                <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
            </#list>
        </#if>
        <title>${msg("loginTitle",(realm.displayName!''))}</title>
        <link rel="icon" href="${url.resourcesPath}/img/favicon.ico?v${properties.kcThemeVersion!}" />
        <#if properties.styles?has_content>
            <#list properties.styles?split(' ') as style>
                <link href="${url.resourcesPath}/${style}?v${properties.kcThemeVersion!}" rel="stylesheet" />
            </#list>
        </#if>
        <#if properties.scripts?has_content>
            <#list properties.scripts?split(' ') as script>
                <script src="${url.resourcesPath}/${script}?v${properties.kcThemeVersion!}" type="text/javascript"></script>
            </#list>
        </#if>
        <#if scripts??>
            <#list scripts as script>
                <script src="${script}" type="text/javascript"></script>
            </#list>
        </#if>
    </head>

  	<body>
		<div class="login-page">
			<div class="wrapper">
				<section class="bottom-logo"></section>
				<section class="login">
					<div class="login__header">
						<span class="login__header-name">
							${msg("doLogIn")}
						</span>
					</div>
					<div class="login__body">
						<form id="kc-form-login" 
					          onsubmit="login.disabled = true; return true;" 
							  action="${url.loginAction}" 
							  method="post">
							<div class="form-group">
								<label for="user-name">${msg("usernameOrEmail")}</label>
								<input tabindex="1" 
								       id="user-name" 
									   class="form-input" 
									   name="username" 
									   value="${(login.username!'')}"
									   type="text" 
									   autocomplete="off" />
							</div>
							<div class="form-group">
								<label for="user-password">${msg("password")}</label>
								<input tabindex="2" 
								       id="user-password" 
									   class="form-input" 
									   name="password" 
									   type="password" 
									   autocomplete="off" />
							</div>
							<#if message?has_content>
								<div class="form-group form-group__incorrect-input">
									<div class="incorrect-input__message">${message.summary?no_esc}</div>
								</div>
							</#if>
							<#if realm.rememberMe??>
								<div class="form-group form-group__checkbox">
									<#if login.rememberMe??>
										<input tabindex="3" 
										       id="rememberMe" 
											   name="rememberMe" 
											   type="checkbox" 
											   class="checkbox" 
											   checked>
									<#else>
										<input tabindex="3" 
										       id="rememberMe" 
											   name="rememberMe" 
											   type="checkbox" 
											   class="checkbox">
									</#if>
									<label for="rememberMe" class="custom-control">
										<span class="custom-control__label">${msg("rememberMe")}</span>
									</label>
								</div>
							</#if>
							<div class="form-group form-group__last-item">
								<button class="btn btn-default w-100" 
								        tabindex="4" 
										id="kc-login"
										name="login"
										type="submit">
									${msg("doLogIn")}
								</button>
							</div>
						</form>
					</div>
				</section>
				<#include "footer.ftl">
			</div>
		</div>
		<script>
			var userName = document.getElementById('user-name'),
				password = document.getElementById('user-password');
			if (userName && userName.value) {
				password && password.focus();
			}
			else {
				userName && userName.focus()
        }
    </script>
	</body>
	
</html>
