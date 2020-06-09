#!/bin/bash
# BUILD_DEPLOY cinibe
# by Luca Sessa
#

# path attuale: ../ da dove si trova questo script
cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. 
DIR="`pwd`"

dtformat="%Y%m%d-%H%M%S"
datetime="`date +$dtformat`"

folderName="cinibe-all"
zipName="cinibe-all-$datetime.zip"
destFolder="$DIR/../Build/cinibe/dist/"
dest="$destFolder$folderName"

echo " "
echo "Crea deploy nuova versione cinibe"
echo " "
mkdir -p $dest
rm -rf $dest
mkdir -p $dest


echo "copying cinibe..."
cd $DIR
mkdir -p $dest/cinibe
# echo "cp -Rf app $dest/cinibe/app"

cp -Rf app $dest/cinibe/app
cp -Rf public $dest/cinibe/public
cp -Rf tools $dest/cinibe/tools
cp -Rf modules $dest/cinibe/modules
cp -Rf lib $dest/cinibe/lib
cp -Rf conf $dest/cinibe/conf
cp -Rf db $dest/cinibe/db


echo "copying eSecurePlay1..."
cd $DIR/../eSecurePlay1
mkdir -p $dest/eSecurePlay1
cp -Rf app $dest/eSecurePlay1/app
cp -Rf lib $dest/eSecurePlay1/lib
cp -Rf conf $dest/eSecurePlay1/conf


echo "continua? (ctrl+c per fermare)"
read


cd $destFolder
destFolder="`pwd`/"
echo "prepare zip $destFolder$zipName ..."


cd $destFolder
zip -qr $zipName $folderName

echo "zip file: $destFolder$zipName"
echo " "
echo "fine creazione deploy cinibe"
echo " "
