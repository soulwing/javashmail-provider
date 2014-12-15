javashmail-provider
===================

A JavaMail provider with useful shims and shams for application testing.  When
testing applications, it is often necessary to prevent e-mail messages (e.g. 
notifications) from being delivered to real recipients.  This JavaMail provider
allows you to simply write sent messages to a file (instead of delivering them)
or to direct messages to fixed recipient (instead of delivering them to the
actual recipients).

Like any JavaMail provider `javashmail-provider` can be used directly by simply
creating a session with the appropriate configuration properties.  It can 
also be used in a Servlet container (e.g. Tomcat) or full Java EE container 
(e.g. Wildfly) to create a mail session as a JNDI resource that can be accessed 
by your application.

Installation
------------

The `javashmail-provider` can be easily installed in your container.

### Tomcat Installation

Copy the `javashmail-provider.x.y.z.jar` file into Tomcat's `lib`
directory.  Also make sure that JavaMail's `mail.jar` and `activation.jar` are
in Tomcat's `lib` directory.

Make sure that your application's `WEB-INF/lib` does not include 
`mail.jar` or `activation.jar` or you will experience classloader mishaps such 
as `ClassNotFoundException` or `NoClassDefFoundError` at runtime.

### Wildfly/JBossAS Installation

The `javashmail-provider-x.y.z-modules.tar.gz` file located in the 
`target` folder (after a successful build using Maven) contains a Wildfly/JBossAS
module.  Untar this file inside of the top-level directory of your server 
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


File Provider
-------------

The File Provider is a JavaMail transport provider that simply writes sent
messages to a file.

### Configuration Properties

These properties must be set on the `javax.mail.Session` object in order to 
use the file-based transport.

* `mail.transport.protocol` -- set this to `file`
* `mail.file.path` -- path to the file to which sent messages will be
  written
* `mail.file.append` -- `true|false` to indicate whether sent messages
  should be appended to the end of the file (or should overwrite the file)

### Direct Use

Make sure your classpath includes `javashmail-provider.x.y.z.jar` and 
JavaMail's `mail.jar` and `activation.jar`.  Then, write code that configures
properties, creates a session, and uses it to send messages to the file.

```
import java.util.Properties;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

public class FileProviderDemo {

  public static void main(String[] args) throws Exception {
    Properties properties = new Properties();
    properties.setProperty("mail.transport.protocol", "file");
    properties.setProperty("mail.file.path", "/tmp/mail.txt");
    properties.setProperty("mail.file.append", "true");
    
    Session session = Session.getInstance(properties);
    
    MimeMessage message = new MimeMessage(session);
    message.setFrom(new InternetAddress("nobody@nowhere.net"));
    message.addRecipient(RecipientType.TO, 
        new InternetAddress("somebody@somewhere.net"));
    message.setSubject("Hello!");
    message.setText("A message from JavaShmail.");
    
    Transport transport = session.getTransport();
    transport.sendMessage(message, message.getAllRecipients());
  }  
}
```

### Tomcat Configuration

Add a resource definition to your application's `<Context>` configuration, as
follows:
```
<Context>
  ...
  <Resource name="mail/Session" auth="Container"
            type="javax.mail.Session"
            mail.transport.protocol="file" 
            mail.file.path="/path/to/a/file/for/sent/messages" 
            mail.file.append="true"/>
  ...
</Context>
```

### Wildfly/JBossAS Configuration

Add to your configuration a mail session that uses the file-based 
transport.  You can do this using the management console, the CLI, or by
editing the XML configuration (`standalone.xml` or `domain.xml`).  For 
brevity's sake, only the XML configuration is shown here.

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
  xmlns:jboss="http://www.jboss.com/xml/ns/javaee" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd">

  <resource-ref>
    <res-ref-name>mail/Session</res-ref-name>
    <res-type>javax.mail.Session</res-type>
    <lookup-name>java:jboss/mail/File</lookup-name>
  </resource-ref>

</jboss:jboss-web>
```

Fixed Recipient Provider
------------------------

The Fixed Recipient Provider is a JavaMail transport provider that directs all
sent messages to one or more fixed recipients, ignoring any recipient addresses
specified in the call to the `sendMessage` method on the `Transport` class.  Because only 
the envelope recipients manipulated, the message recipients in the message header 
are unaffected.  This allows you to deliver all messages to a fixed mailbox or 
(mailboxes) while still having the original recipient headers intact to inspect 
for correctness.

This provider doesn't perform the actual message delivery, it delegates to 
another transport provider (e.g. the `smtp` provider), by performing a JNDI 
lookup to locate it.  As such, the Fixed Recipient Provider is intended to be 
used in a Java EE or Servlet container.

### Configuration Properties

These properties must be set on the `javax.mail.Session` object in order to 
use the fixed recipient transport.

* `mail.transport.protocol` -- set this to `rcpt`
* `mail.rcpt.delegate` -- fully-qualified JNDI name for `Session` resource for
   the transport delegate (usually using the `smtp` transport)
* `mail.rcpt.address` -- address that will be used as the envelope recipient for
  all messages sent via the fixed recipient transport provider (this can be more
  than one address using any legitimate RFC-822 address syntax such as would be
  used in a `To:` header

### Tomcat Configuration

In your application's `<Context>` configuration, you will typically define two 
JNDI resources of type `javax.mail.Session`. The first is an ordinary mail session 
supporting `smtp` such as shown in the Tomcat's [JNDI Resources HOW-TO] 
(http://tomcat.apache.org/tomcat-6.0-doc/jndi-resources-howto.html#JavaMail_Sessions)
guide.  The second resource is the Fixed Recipient provider.

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
