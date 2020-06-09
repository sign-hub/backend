#!/bin/bash

dtformat="%Y%m%d-%H%M%S"
datetime="`date +$dtformat`"

folder=/usr/cini
bak=/usr/cini/bak
play=/usr/srv/play-1.4.4/play

if ["$1" == ""]
then
        echo "usage: "
        echo "$0 cinibe-all....zip"
        exit 1
fi



echo "unzipping... "
unzip $1

echo "invio per continuare (ctrl-c per fermare)?"
read

echo " "
echo "installing cinibe..."
echo " "
$folder/bin/cinibe-shutdown.sh

old=$bak/cinibe-old-$datetime

if [ -d $folder/cinibe-app ]
then
        echo "creating backup in $old ..."
        mkdir -p $old
        mv $folder/cinibe-app/* $old/
fi

mkdir -p $folder/cinibe-app


mv cinibe-all/* $folder/cinibe-app/
rmdir cinibe-all

if [ "$2" == "evolutions" ]
then
        echo "EVOLUZIONE DATABASE IN CORSO.."
        cd $folder/cinibe-app/cinibe
        $play evolutions:apply --%prod
fi

$folder/bin/cinibe-startup.sh recompile

echo " "
echo "fine installazione."