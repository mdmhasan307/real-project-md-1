#!/bin/bash

set -e
# keep track of the last executed command
trap 'last_command=$current_command; current_command=$BASH_COMMAND' DEBUG
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

CODE_DIR=`pwd`
CUR_BRANCH=`git rev-parse --abbrev-ref HEAD`
CUR_COMMIT=`git rev-parse HEAD`
CUR_DATE=`date "+%Y%m%d"`
STRATIS_VERSION="704.01.05"
STRATIS_ARCHIVE_NAME="stratis-src-$CUR_DATE-v$STRATIS_VERSION-$CUR_COMMIT.zip"

SRC_EXPORT_DIRNAME_ONLY="stratis-src-$CUR_DATE-$CUR_COMMIT"

SRC_EXPORT_DIR="/var/tmp/$SRC_EXPORT_DIRNAME_ONLY"

####################################################
rm -rf $SRC_EXPORT_DIR

mvn clean compile package

mkdir $SRC_EXPORT_DIR

git archive $CUR_BRANCH | tar -x -C $SRC_EXPORT_DIR

echo $CUR_COMMIT > $SRC_EXPORT_DIR/git.commit
echo $CUR_BRANCH > $SRC_EXPORT_DIR/git.branch

if [ -d wars ]; then
    echo "Removing old WARS directory..."
    rm -rf wars/
fi
mkdir wars

cd wars
mkdir gcssmc
mkdir application

unzip ../GCSSMCWS/target/gcss*.war -d gcssmc/
unzip ../application/target/stratis*.war -d application/

cp -r gcssmc/WEB-INF/lib/ $SRC_EXPORT_DIR/GCSSMCWS/src/main/webapp/WEB-INF/
cp -r application/WEB-INF/lib/ $SRC_EXPORT_DIR/application/public_html/WEB-INF/

cd $SRC_EXPORT_DIR
rm -rf $SRC_EXPORT_DIR/.data
rm -rf $SRC_EXPORT_DIR/tomcat8
rm $SRC_EXPORT_DIR/.gitignore
rm $SRC_EXPORT_DIR/.gitlab-ci.yml
rm $SRC_EXPORT_DIR/sonar-project.properties
find $SRC_EXPORT_DIR/ -type f -name pom.xml -exec rm {} \;
rm $SRC_EXPORT_DIR/COMApplet/COMApplet.jpr
rm $SRC_EXPORT_DIR/EXMLService/src/exmlservice/gcssmc_cer/keystore.jks
rm $SRC_EXPORT_DIR/src/META-INF/keystore/stratweb.jks
rm $SRC_EXPORT_DIR/src/META-INF/cwallet*
rm $SRC_EXPORT_DIR/application/public_html/applet/comapplet/*.class
rm $SRC_EXPORT_DIR/application/public_html/applet/*.cer
rm -rf $SRC_EXPORT_DIR/application/src/mil/test
rm -rf $SRC_EXPORT_DIR/fortify-workspace
rm -rf $SRC_EXPORT_DIR/application/test

cd /var/tmp

zip -r stratis-src-$CUR_DATE-v$STRATIS_VERSION-$CUR_COMMIT.zip $SRC_EXPORT_DIRNAME_ONLY
md5 -r $STRATIS_ARCHIVE_NAME > $STRATIS_ARCHIVE_NAME.md5
rm -rf $SRC_EXPORT_DIRNAME_ONLY

