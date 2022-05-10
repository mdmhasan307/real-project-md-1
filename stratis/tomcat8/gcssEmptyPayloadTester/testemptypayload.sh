#!/bin/bash

#for a dev machine
BASEDIR="/home/moodm/code/mls2/stratisweb-redux/application/target/view-controller-704.01.01/WEB-INF"
BASETOMCATDIR="/opt/apache-tomcat-8.5.43"

#for a 704 server
#BASEDIR="/opt/app/stratis/tomcat/current/webapps/stratis/WEB-INF"
#BASETOMCATDIR="/opt/app/stratis/tomcat/current"

CLASSPATH="./ExportI142EmptyPayloadTester.jar:./efxlic_stratisintegration12.jar:$BASEDIR/lib/*:$BASEDIR/RICE_INTF/:$BASEDIR/classes/:$BASETOMCATDIR/lib/*:$BASETOMCATDIR/bin/*"

EXTRA_OPTS="-Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl"
EXTRA_OPTS="$EXTRA_OPTS -Djavax.xml.transform.TransformerFactory=com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"
EXTRA_OPTS="$EXTRA_OPTS -Djavax.xml.parsers.SAXParserFactory=com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl"
EXTRA_OPTS="$EXTRA_OPTS -Djavax.xml.datatype.DatatypeFactory=com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl"
#EXTRA_OPTS="$EXTRA_OPTS -Djavax.net.debug=all"
EXTRA_OPTS="$EXTRA_OPTS -Dcom.sun.xml.ws.transport.http.client.HttpTransportPipe.dump=true"
EXTRA_OPTS="$EXTRA_OPTS -Djavax.net.debug=ssl"

echo java $EXTRA_OPTS -cp "$CLASSPATH" mil.stratis.model.threads.imports.ExportI142EmptyPayloadTester ./testemptypayload.conf

#for a dev machine - something like this or similar
JAVA_EXE="/usr/local/java/jdk1.8.0_221/bin/java"

#for a 704 server
#JAVA_EXE="/usr/java/default/bin/java"

$JAVA_EXE $EXTRA_OPTS -cp "$CLASSPATH" mil.stratis.model.threads.imports.ExportI142EmptyPayloadTester ./testemptypayload.conf
#java -cp "$CLASSPATH" mil.stratis.model.threads.imports.ExportI142EmptyPayloadTester
