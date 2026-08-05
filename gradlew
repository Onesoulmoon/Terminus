#!/bin/sh

##############################################################################
# Gradle start up script for POSIX
##############################################################################

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

APP_HOME=$(cd "$(dirname "$0")" && pwd)

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
    "-Dorg.gradle.appname=$(basename "$0")" \
    -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
    org.gradle.wrapper.GradleWrapperMain "$@"
