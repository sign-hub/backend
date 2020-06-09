#!/bin/bash

app=cinibe-app
startApp=cinibe
play=/usr/srv/play-1.4.4/play

cd "$( dirname "${BASH_SOURCE[0]}" )" && cd ../$app
DIR="`pwd`"

cd $DIR/$startApp
$play stop --%prod
sleep 2
rm -f server.pid