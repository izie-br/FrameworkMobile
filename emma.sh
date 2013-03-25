#!/bin/sh

RUN_MVN='mvn clean install -e -Pemma'

EMMA_JAR=~/.m2/repository/emma/emma/2.1.5320/emma-2.1.5320.jar
INPUT_FILES='lib/target/coverage.em,lib_jdbc/target/coverage.em,lib_android/target/coverage.em,test/target/coverage.em,lib/coverage.ec,lib_jdbc/coverage.ec,lib_android/coverage.ec,test/target/emma/coverage.ec'
SOURCE_FOLDERS='lib/src/main/java,lib_android/src/main/java,lib_jdbc/src/main/java,test/src/main/java'
RUN_EMMA="java -cp $EMMA_JAR emma report -r html -in $INPUT_FILES -sp $SOURCE_FOLDERS"

echo $RUN_MVN
$RUN_MVN
echo 'cd test'
cd test
echo $RUN_MVN
$RUN_MVN
echo 'cd ..'
cd ..
echo $RUN_EMMA
$RUN_EMMA
