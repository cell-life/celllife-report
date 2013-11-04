#!/bin/sh

BASE_DIR=`dirname $0`

. $BASE_DIR/setenv.sh

rm -rf $BASE_DIR/target

mvn clean install -f $BASE_DIR/../pom.xml
