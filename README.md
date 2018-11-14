# Skyglass Query - declarative query builder framework

* angular-spring-security is a demo project which shows how to implement following security features:

1. User Login Form Authentication, with Skyglass & Spring Security 
2. User Remember Me Authentication with Cookies and Persistent Tokens, using Skyglass & Spring Security
7. Angular State protection, using 'angular-permission'
5. Angular State Permission Management, with Skyglass Security
4. Menu Tab and Action protection, with Skyglass Security
5. Menu Tab and Action Permission Management, with Skyglass Security
5. User redirection to bookmarked or home page after successful login, with Skyglass Security
6. Dynamic Angular State and Tab Link generation, depending on user permissions, with Skyglass Security
7. Redirection to 'No Permissions' page for unauthorized users, with Skyglass Security
8. Redirection to 'Login' page for non-authenticated users, with Skyglass Security
9. Breadcrumbs auto generation, depending on current angular state, with uiBreadcrumbs directive
10. User Logout, with Skyglass & Spring Security

The project is based on Angular JS, Skyglass Security, Angular UI-Router, angular-permission, uiBreadcrumbs directive, Spring Boot, Spring Security, Spring REST, Spring Data JPA and HSQLDB

#angular-spring-security installation

* Run 'mvn clean install' or import 'angular-spring-security' maven project to your IDE

#angular-spring-security Configuration

* Test users and their permissions are created in 'src/test/resource/data.sql'. 
* User with read-only permissions - login: audit, password: audit
* User with write permissions - login: admin, password: admin
* All javascript modules are defined in 'src/main/resources/static/js/require.config.js' and loaded by RequireJS.
* All angular states are defined in 'src/main/resources/static/js/modules/app.js' file. Any non-abstract state shoud have 'data.displayName' property which is used by uiBreadcrumbs directive to automatically generate breadcrumbs. Any non-abstract state may have 'data.permissions' property which is used by angular-permission library to protect the angular state and redirect unauthorized user to 'No Permissions' page. Define your own states in this file to enable state protection and breadcrumbs auto generation in your own application. See https://github.com/angular-ui/ui-router/wiki/nested-states-%26-nested-views for more details on angular-ui-router nested states & nested views.

# Skyglass Security Configuration

* 'src/main/resources/static/security' - Skyglass Security Module folder
* 'src/main/resources/static/js/security/config' - Skyglass Security Config folder. All files in this folder should be changed in order to enable Skyglass Security in your own Application.
* 'src/main/resources/static/js/security/config/security.config.js' contains basic settings like 'loginPath', 'authenticateUrl', 'rememberMeAuthenticateUrl', 'logoutUrl' and so on
* 'src/main/resources/static/js/security/config/security.permissions.js' contains USER_ROLES constants and PERMISSIONS properties. Change these objects to define your own permissions. USER_ROLES correspond to role names on server. PERMISSIONS properties are used by 'security.menu.config.js' to protect menu tabs and by 'security.state.permissions.js' to protect angular states
* 'src/main/resources/static/js/security/config/security.menu.config.js' contains menu tabs configuration. Any menu tab may have 'permission' property (or array of properties) to define authorization rules. Unauthorized user won't be able to see protected tab. Names of menu tab 'permission' properties correspond to PERMISSIONS object properties of the 'security.permissions.js' file.
- 'src/main/resources/static/js/security/config/security.state.permissions.js' defines permissions for angular state protection, with 'angular-permission'. Unauthorized user won't be able to go to protected state and will be redirected to 'No Permissions' state. 

# Skyglass Security API
* $securitySession.permissions - returns permissions defined in security.permission.js file
* $securitySession.user() - returns user object. Currently, only 'name' property is supported. Override SESSION.initData() method in security.session.js file to fill other properties returned from server.
* $securityMenuConfig[{menuName}].tabs - returns tabs defined in security.menu.config.js file
* $securityMenuConfig[{menuName}].{property} - returns any other property defined in security.menu.config.js file
* $securityMenuConfig.defaultAdminState() - returns dynamic state which depends on user permissions. For example, for user 'admin' it would be 'skyglass.admin.write' state, but for user 'audit' it would be 'skyglass.admin.read' state.
* $securityService.login(credentials, callback) - this method is called when user clicks 'submit' button on the login form. Parameter 'success' is pased to callback function. Default implementation of the callback function shows error message to user if login is unsuccessful and reloads main menu if login is successful
* $securityService.authorizeMenu($securityMenuConfig[[menuName]) - loads menu by name {menuName}. The menu is defined in security.menu.config.js file. Each menu tab may have 'permission' property which corresponds to PERMISSIONS defined in security.session.js file. According to the value of 'permissions' property, the tab chooses to render itself or not.
* $securityServiceProvider.rememberMeAuthenticate($windowProvider.$get().location.pathname) - this method should be called before angular states are defined, in the config block of the main module: 'src/main/static/js/modules/app.js'. This allows to take into account user permissions when angular dynamic states are configured.
* $securityService.start() - initializes security module and redirects to the bookmarked, home or login state
* security-menu directive - loads menu by name. The menu is defined in security.menu.config.js. The menu tabs are rendered according to user permissions.

# Skyglass Security UI
2. Run skyglass.demo.SkgApplication java class
3. Go to localhost:8080/{any_friendly_url}. All urls are 'friendly'. No more #anchors!
4. You will be redirected to login page with 'remember me' checkbox
5. Login as 'admin' or 'audit'
6. You will be redirected to {any_friendly_url} or 'Home' state.
7. Click on 'Admin' tab: you will be automatically redirected to 'Admin -> Write' or 'Admin -> Read' state depending on user permissions. If you want to change the way breadcrumbs are generated and want them to look like "Write" instead of "Admin -> Write" then you should define 'data.proxy' property instead of 'data.proxyLink' property and make 'skyglass.admin' state abstract. See 'skyglass.users' state and correspondent 'Users' menu link as an example. (Don't confuse with the similarly named 'skyglass.admin.security.users' state and its corresponsent 'Admin -> Security -> Users' tab link)
8. Click on 'Admin -> Security' tab: you will be automatically redirected to 'Admin -> Security -> Users' state. This state has úsers table, roles table and remember-me tokens table. To switch between these tables use correspondent 'select' control. This control changes 'breadcrumbs' look on selection of the new control value.
9. Admin -> Security -> Users - here you can view, order and filter users list, create new user, edit existing user, manage user roles in a multiple selection dialog window, reset user password and delete user
10. Admin -> Security -> Roles - here you can view, order and filter roles list, add new role, edit existing role and delete it.
11. Admin -> Security -> Tokens - here you can view, order and filter tokens list and and delete existing tokens. Remember-Me Tokens contain information about persistent user sessions, including IP Address and Browser.
