#!/bin/sh

mvn clean install -Pemma
java -cp ~/.m2/repository/emma/emma/2.1.5320/emma-2.1.5320.jar emma report -r html -in lib_jdbc/target/coverage.em,lib_android/target/coverage.em,test/target/coverage.em,lib_jdbc/coverage.ec,lib_android/coverage.ec,test/target/emma/coverage.ec -sp lib_android/src/main/java,lib_jdbc/src/main/java,test/src/main/java
