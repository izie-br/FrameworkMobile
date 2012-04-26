#!/bin/sh

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
SCHEMA="$DIR/script/schema.sql"
GENDIR="$DIR/customGen"
MAIN=br.com.cds.mobile.geradores.GeradorDeBeans
JARS="$DIR/target/gerador-0.2-alpha.jar:$DIR/libs/jsqlparser.jar:$DIR/libs/codemodel-2.4.jar:$DIR/../lib/target/libFramework-0.2-alpha.jar:$DIR/libs/android.jar"

/bin/sh $DIR/script/gerar_schema.sh
java -classpath $JARS $MAIN $SCHEMA br.com.cds.mobile $GENDIR

