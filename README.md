javamail-file-provider
======================

A JavaMail Provider for a Transport that appends sent messages to a file.  
This provider is useful for testing applications that send mail.

Configuration Properties
------------------------

These properties must be set on the ```javax.mail.Session``` object in order to 
use the file-based transport.

* `mail.transport.protocol` -- set this to `file`
* `mail.file.path` -- path to the file to which sent messages will be
  written
* `mail.file.append` -- `true|false` to indicate whether sent messages
  should be appended to the end of the file (or should overwrite the file)

Tomcat Configuration
--------------------

Copy the `javamail-file-provider.x.y.z.jar` file into Tomcat's `lib`
directory.  Also make sure that JavaMail's `mail.jar` is in Tomcat's 
`lib` directory.

Make sure that your application's `WEB-INF/lib` does not include 
`mail.jar`.

Add a global naming resource that uses the file-based transport provider in
your `server.xml`.

```
<Server>
  ...
  <GlobalNamingResources>
    <Resource name="FileMailSession" auth="Container"
              type="javax.mail.Session"
              mail.transport.protocol="file" 
              mail.file.path="/path/to/a/file/for/sent/messages" 
              mail.file.append="true"/>
  </GlobalNamingResources>
  ...
</Server>
```

Add a link to the mail resource to your application's context configuration:

```
<Context>

  <ResourceLink name="mail/Session"
    global="FileMailSession"
    type="javax.mail.Session"/>

</Context>
```

Wildfly/JBossAS Configuration
-----------------------------

Untar the `javamail-file-provider-x.y.z-modules.tar.gz` file located in the 
`target` folder of the build inside of the top-level directory of your
Wildfly/JBossAS server installation.  This will install the provider as a
module.

Edit your configuration (either `standalone.xml` or `domain.xml`) to
make the provider module available to all deployments in the container.

```
<subsystem xmlns="urn:jboss:domain:ee:2.0">
  ...
  <global-modules>
    <module name="org.soulwing.mail" slot="main"/>
  </global-modules>
  ...
</subsystem>
```

Also add to your configuration a mail session that uses the file-based 
transport.

```
<subsystem xmlns="urn:jboss:domain:mail:2.0">
   ...
   <mail-session name="file" jndi-name="java:jboss/mail/File">
     <custom-server name="FileTransport">
       <property name="mail.transport.protocol" value="file"/>
       <property name="mail.file.path" value="/path/to/a/file/for/sent/messages"/>
       <property name="mail.file.append" value="true"/>
     </custom-server>
   </mail-session>
   ...
</subsystem>
```

Add a resource reference for the mail resource to your application's 
`jboss-web.xml` descriptor.

```
<?xml version="1.0" encoding="UTF-8"?>
<jboss:jboss-web xmlns="http://xmlns.jcp.org/xml/ns/javaee"
  xmlns:jboss="http://www.jboss.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd">

  <resource-ref>
    <res-ref-name>mail/Session</res-ref-name>
    <res-type>javax.mail.Session</res-type>
    <lookup-name>java:jboss/mail/File</lookup-name>
  </resource-ref>

</jboss:jboss-web>
```

 


