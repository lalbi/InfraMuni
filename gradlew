#!/usr/bin/env bash
# (c) Gradle Inc. All rights reserved.

set -o errexit
set -o nounset
set -o pipefail

# Resolve links ($0 may be a symlink)
PRG="$0"
while [ -h "$PRG" ] ; do
    ls=$(ls -ld "$PRG")
    link=$(expr "$ls" : '.*-> \(.*\)$')
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=$(dirname "$PRG")/"$link"
    fi
done

APP_HOME=$(dirname "$PRG")

# Use JAVA_HOME if set, otherwise look for java in PATH
if [ -n "${JAVA_HOME:-}" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD=java
fi

# Execute Gradle Wrapper
exec "$JAVA_CMD" \
  -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"
