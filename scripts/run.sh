#!/usr/bin/env bash

CONFIG_FILE="conf/application-dev.conf"

if [ -n "$1" ]
then
    CONFIG_FILE="$1"
fi

echo "Starting Housekeeper application with config $CONFIG_FILE"
echo ""
./sbt "run -Dconfig.file=$CONFIG_FILE"
