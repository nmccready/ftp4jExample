#!/bin/sh
exec java -Xms512m -Xmx512m -Xmx1024M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=1024M -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=6969 ${SBT_OPTS} -jar ./sbt-launch.jar
