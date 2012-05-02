#!/bin/sh

if [ $# -lt 1 ]; then
  echo "Uso: $0 <pasta_base_projeto>"
  exit
fi

#DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
GERADOR_SCRIPT_DIR="$( cd -P "$( dirname "$0" )" && pwd )"

BASE_DIR="$1"

SCHEMA="$BASE_DIR/script/schema.sql"
GENDIR="$BASE_DIR/customGen"

MAIN=br.com.cds.mobile.geradores.GeradorDeBeans
JARS="$GERADOR_SCRIPT_DIR/../target/gerador-0.2-alpha.jar:$GERADOR_SCRIPT_DIR/../libs/jsqlparser.jar:$GERADOR_SCRIPT_DIR/../libs/codemodel-2.4.jar:$GERADOR_SCRIPT_DIR/../../lib/target/libFramework-0.2-alpha.jar:$GERADOR_SCRIPT_DIR/../libs/android.jar"

SQL_XML="$BASE_DIR/res/values/sql.xml"

DB_TESTE="$BASE_DIR/script/teste.db"
SCHEMA="$BASE_DIR/script/schema.sql"

cat "$SQL_XML" |
       #TODO deixar o nome como argumento do script
       xpath '//string[contains(@name,"db_versao_")]/text()' |
       #TODO conferir a versao apos db_versao 
       # escrevendo todos scripts no sqlite3
       sqlite3 $DB_TESTE

sqlite3 $DB_TESTE '.schema' > $SCHEMA

rm $DB_TESTE

PACKAGE="$(
    cat $BASE_DIR/AndroidManifest.xml |
        awk  '/<manifest/,/>/' |
        awk '/package/,//' |
        sed -E 's/.*package="([^"]*)".*/\1/'
)"
echo $PACKAGE

java -classpath $JARS $MAIN $SCHEMA $PACKAGE $GENDIR

