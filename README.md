javamail-file-provider
======================

A JavaMail Provider for a Transport that appends sent messages to a file.  
This provider is useful for testing applications that send mail.

Configuration Properties
------------------------

* ```mail.transport.protocol``` -- set this to ```file```
* ```mail.file.path``` -- path to the file to which sent messages will be
  written
* ```mail.file.append``` -- ```true|false``` to indicate whether sent messages
  should be appended to the end of the file (or should overwrite the file)
