#!/bin/sh
APP_HOME=${0%/*}
APP_NAME=$(basename $0)
APP_BASE_NAME=${APP_NAME%.*}
if [ "$APP_HOME" = "$APP_NAME" ]; then APP_HOME=.; fi
if [ -n "$JAVA_HOME" ] ; then
    JAVA_EXE="$JAVA_HOME/bin/java"
else
    JAVA_EXE="java"
fi
if [ ! -x "$JAVA_EXE" ] ; then
    echo "Error: JAVA_HOME is not set or $JAVA_EXE does not exist." >&2
    exit 1
fi
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec "$JAVA_EXE" -Dorg.gradle.appname="$APP_BASE_NAME" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
