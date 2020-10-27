javashmail-provider
===================

[![Build Status](https://travis-ci.org/soulwing/javashmail-provider.svg?branch=master)](https://travis-ci.org/soulwing/javashmail-provider)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.soulwing/javashmail-provider/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.soulwing%20a%3Ajavashmail-provider*)


A JavaMail provider with useful shims and shams for application testing.  When
testing applications, it is often necessary to prevent e-mail messages (e.g. 
notifications) from being delivered to real recipients.  This JavaMail provider
allows you to simply write sent messages to a file (instead of delivering them)
or to direct messages to a fixed recipient (instead of delivering them to the
actual recipients).

Like any JavaMail provider `javashmail-provider` can be used directly by simply
creating a session with the appropriate configuration properties.  It can 
also be used in a Servlet container (e.g. Tomcat) or full Java EE container 
(e.g. Wildfly) to create a mail session as a JNDI resource that can be accessed 
by your application. For direct use, make sure your classpath includes 
`javashmail-provider.x.y.z.jar` and  JavaMail's `mail.jar` and `activation.jar`.  
Then, write code that configures properties, creates a session, and uses it to
send message.

Installation in Servlet or Java EE Container
--------------------------------------------

The `javashmail-provider` can be easily installed in your container.  The 
necessary artifacts can be installed by downloading them from 
[Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.soulwing%20a%3Ajavashmail-provider).

### Tomcat Installation
Copy the `javashmail-provider.x.y.z.jar` file into Tomcat's `lib`
directory.  Also make sure that JavaMail's `mail.jar` and `activation.jar` are
in Tomcat's `lib` directory.

Make sure that your application's `WEB-INF/lib` does not include 
`mail.jar` or `activation.jar` or you will experience classloader mishaps such 
as `ClassNotFoundException` or `NoClassDefFoundError` at runtime.

### Wildfly/JBossAS Installation

The `javashmail-provider-x.y.z-modules.tar.gz` contains a Wildfly/JBossAS
module. 

Untar this file inside of the top-level directory of your server 
installation.  This will properly install the provider in the `modules` 
subdirectory.

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

Available Transports
====================

This module provides several useful transports that you can use in various 
situations, and which are especially useful for integration testing.

* [Null Transport](#null-transport)
* [File Transport](#file-transport)
* [Timeout Transport](#timeout-transport)
* [Fixed Recipient Transport](#fixed-recipient-transport)

Null Transport
-------------

The Null Transport discards all messages sent to it. It has no configuration 
properties other than the protocol name required by JavaMail to select it. 
This transport is principally useful as a way to suppress sending mail. For
example, you might use it in a application deployment in which you don't want 
to send email at all.

### Configuration Properties

These properties must be set on the `javax.mail.Session` object in order to 
use the Null Transport.

* `mail.transport.protocol` -- set this to `null`


File Transport
--------------

The File Transport simply writes sent messages to a file.

### Configuration Properties

These properties must be set on the `javax.mail.Session` object in order to 
use the File Transport.

* `mail.transport.protocol` -- set this to `file`
* `mail.file.path` -- path to the file to which sent messages will be
  written
* `mail.file.append` -- `true|false` to indicate whether sent messages
  should be appended to the end of the file (or should overwrite the file)


Timeout Transport
----------------
The Timeout Transport can simulate timeouts for when connecting or sending
a message. A call to `connect` or `sendMessage` on the transport will put
the calling thread to sleep for the specified and then throw a 
`MessagingException`.  

### Configuration Properties

These properties must be set on the `javax.mail.Session` object in order to 
use the Timeout Transport.

* `mail.transport.protocol` -- set this to `timeout`
* `mail.timeout.connectionTimeout` -- set this to a value greater 0 to simulate a timeout during connection creation
* `mail.timeout.messageTimeout` -- set this to a value greater 0 to simulate a timeout during message sending
* `mail.timeout.delegate.transport.protocol` -- set this to the name of a delegate transport transport

As indicated in the above properties, the timeout transport is intended to be 
used with another transport to which it delegates when no timeout is configured.
For example, you could configure either the File transport or the Null 
transport for this purpose. When specifying a delegate, you can provide 
properties to the delegate by prefixing them appropriately. For example, to use
the File transport as the delegate for the timeout transport, you could 
specify configuration properties as follows:

```
mail.transport.protocol=timeout
mail.timeout.connectionTimeout=0
mail.timeout.messageTimeout=5
mail.timeout.delegate.transport.protocol=file
mail.timeout.delegate.file.path=/some/path/to/the/target/file
```

Fixed Recipient Transport
------------------------

The Fixed Recipient Transport directs all sent messages to one or more fixed 
recipients, ignoring any recipient addresses specified in the call to the 
`sendMessage` method on the `Transport` class.  Because only the envelope 
recipients are manipulated, the message recipients in the message header are 
unaffected.  This allows you to deliver all messages to a fixed mailbox or 
(mailboxes) while still having the original recipient  headers intact to 
inspect for correctness.

This transport doesn't perform the actual message delivery. It delegates to 
another transport transport (e.g. the `smtp` transport), by performing a JNDI 
lookup to locate it.  As such, the Fixed Recipient Transport is intended to be 
used in a Java EE or Servlet container.

### Configuration Properties

These properties must be set on the `javax.mail.Session` object in order to 
use the fixed recipient transport.

* `mail.transport.protocol` -- set this to `rcpt`
* `mail.rcpt.delegate` -- fully-qualified JNDI name for a `Session` resource from
  which a transport delegate (usually using the `smtp` transport) can be obtained
* `mail.rcpt.address` -- address(es) that will be used as the envelope recipient for
  all messages sent via the fixed recipient transport transport; more
  than one address can be specified using any legitimate RFC-822 address syntax such 
  as would be used in a `To:` header

### Tomcat Configuration

In your application's `<Context>` configuration, you will typically define two 
JNDI resources of type `javax.mail.Session`. The first is an ordinary mail session 
supporting `smtp` such as shown in the Tomcat's [JNDI Resources HOW-TO](http://tomcat.apache.org/tomcat-6.0-doc/jndi-resources-howto.html#JavaMail_Sessions)
guide.  The second resource is the Fixed Recipient transport.

```
<Context ...>
  ...
  <Resource name="mail/SessionDelegate" auth="Container"
            type="javax.mail.Session"
            mail.transport.protocol="smtp"
            mail.smtp.host="localhost"/>

  <Resource name="mail/Session" auth="Container"
            type="javax.mail.Session"
            mail.transport.protocol="rcpt"
            mail.rcpt.delegate="java:comp/env/mail/SessionDelegate"
            mail.rcpt.address="some-mailbox@example.org"/>
  ...
</Context>
```

In your code that sends mail, you would inject or lookup the `mail/Session` 
resource, and it would delegate to `mail/SessionDelegate` resource after replacing
the real envelope recipients with the fixed value specified here.  In your 
production deployment, you would simply remove the fixed recipient session and
rename the smtp session as `mail/Session` with no need to modify your code.

### Wildfly/JBossAS configuration

Add to your configuration a mail session that uses the fixed recipient transport.
transport.  You can do this using the management console, the CLI, or by
editing the XML configuration (`standalone.xml` or `domain.xml`).  For 
brevity's sake, only the XML configuration is shown here.

```
<subsystem xmlns="urn:jboss:domain:mail:2.0">
   ...
   <mail-session name="rcpt" jndi-name="java:jboss/mail/FixedRecipient">
     <custom-server name="FixedRecipientTransport">
       <property name="mail.transport.protocol" value="rcpt"/>
       <property name="mail.rcpt.delegate" value="java:jboss/mail/Default"/>
       <property name="mail.rcpt.address" value="some-mailbox@example.org"/>
     </custom-server>
   </mail-session>
   ...
</subsystem>
```

Note that the mail session configuration shown here uses the built in default
mail session named `java:jboss/mail/Default`.  The default mail session delivers
SMTP mail to localhost port 25.  Alternatively, you could define your own
delegate session using a different port or protocol, and specify its JNDI name
as the delegate for the fixed recipient transport.

Add a resource reference for the mail resource to your application's 
`jboss-web.xml` descriptor.

```
<?xml version="1.0" encoding="UTF-8"?>
<jboss:jboss-web xmlns="http://xmlns.jcp.org/xml/ns/javaee"
  xmlns:jboss="http://www.jboss.com/xml/ns/javaee" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd">

  <resource-ref>
    <res-ref-name>mail/Session</res-ref-name>
    <res-type>javax.mail.Session</res-type>
    <lookup-name>java:jboss/mail/FixedRecipient</lookup-name>
  </resource-ref>

</jboss:jboss-web>
```

