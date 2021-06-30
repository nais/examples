#!/usr/bin/env bash
set -e

usage() {
  cat <<HEREDOC
Simple script for building and running docker-compose files based on classifier argument
Usage:
  $(basename "${0}") [ktor|tokensupport|springsecurity] [up|down]
HEREDOC
}

checkargs() {
  if [[ "$1" == "ktor" ]] || [[ "$1" == "tokensupport" ]] || [[ "$1" == "springsecurity" ]]
  then
    CLASSIFIER=$1
  else
    usage
    exit 1
  fi
  if [[ "$2" == "up" ]] || [[ "$2" == "down" ]]
  then
    COMMAND=$2
  else
    usage
    exit 1
  fi
}

checkargs "$1" "$2"

FILE_ARGS="-f docker-compose.yml -f docker-compose.login.yml -f docker-compose.$CLASSIFIER.yml"
echo "running docker-compose with: $FILE_ARGS"

if [[ "$COMMAND" == "up" ]]
then
  echo "build and take up services...."
  docker-compose $FILE_ARGS down
  ../../gradlew build -b ../../build.gradle.kts
  docker-compose $FILE_ARGS build
  docker-compose $FILE_ARGS up
fi

if [[ "$COMMAND" == "down" ]]
then
  echo "shutdown services...."
  docker-compose  $FILE_ARGS down
fi