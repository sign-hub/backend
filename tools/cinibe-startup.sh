#!/bin/bash

app=cinibe-app
startApp=cinibe
play=/usr/srv/play-1.4.4/play

cd "$( dirname "${BASH_SOURCE[0]}" )" && cd ../$app
DIR="`pwd`"

cd $DIR/$startApp
rm -f server.pid
if [ "$1" == "recompile" ]
then
   $play precompile --%prod
fi

$play start --%prod -Dprecompiled=true