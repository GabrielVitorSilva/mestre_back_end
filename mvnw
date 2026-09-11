#!/bin/sh

set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec java \
  -Dmaven.multiModuleProjectDirectory="$project_dir" \
  -classpath "$project_dir/.mvn/wrapper/maven-wrapper.jar" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
