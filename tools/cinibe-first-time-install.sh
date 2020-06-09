dtformat="%Y%m%d-%H%M%S"
datetime="`date +$dtformat`"

folder=/usr/cini
app=cinibe-app
appRoot=cinibe
play=/usr/srv/play-1.4.4/play

if ["$1" == ""]
then
        echo "usage: "
        echo "$0 cinibe-all....zip"
        exit 1
fi

mkdir -p $folder/$app

echo "unzipping... "
unzip $1 -d $folder

echo "invio per continuare (ctrl-c per fermare)?"
read

mv cinibe-all/* $folder/$app
rmdir cinibe-all

cd $folder/$app/$appRoot

echo "EVOLUZIONE DATABASE IN CORSO.."

$play evolutions:apply --%prod

$folder/bin/cinibe-startup.sh recompile

echo " "
echo "fine installazione."