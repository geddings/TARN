#!/bin/sh

# Set paths
FL_HOME=`dirname $0`
FL_JAR="${FL_HOME}/target/floodlight.jar"
FL_LOGBACK="${FL_HOME}/logback.xml"

# Create a logback file if required
#[ -f ${FL_LOGBACK} ] || cat <<EOF_LOGBACK >${FL_LOGBACK}
#<configuration scan="true">
#    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
#        <encoder>
#            <pattern>%level [%logger:%thread] %msg%n</pattern>
#        </encoder>
#    </appender>
#    <root level="INFO">
#        <appender-ref ref="STDOUT" />
#    </root>
#    <logger name="org" level=“ALL”/>
#    <logger name="LogService" level=“DEBUG”/> <!-- Restlet access logging -->
#    <logger name="net.floodlightcontroller" level=“ALL”/>
#    <logger name="net.floodlightcontroller.logging" level=“ALL”/>
#</configuration>
#EOF_LOGBACK

echo "Starting floodlight server ..."
java -Dlogback.configurationFile=${FL_LOGBACK} -jar ${FL_JAR} -cf src/main/resources/floodlightdefault.properties &
java -Dlogback.configurationFile=${FL_LOGBACK} -jar ${FL_JAR} -cf src/main/resources/floodlightdefault-2.properties &
