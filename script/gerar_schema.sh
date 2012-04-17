#!/bin/sh

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SQL_XML="$DIR/../res/values/sql.xml"

DB_TESTE="$DIR/teste.db"
SCHEMA="$DIR/schema.sql"

cat "$SQL_XML" |
	# contains no xpath eh equivalente a like %abc% no sql
	#TODO deixar o nome como argumento do script
	xpath '//string[contains(@name,"db_versao_")]/text()' |
	#TODO conferir a versao apos db_versao 
	# removendo as aspas duplas
	sed 's/"//g' |
	# escrevendo todos scripts no sqlite3
	sqlite3 $DB_TESTE

sqlite3 $DB_TESTE '.schema' > $SCHEMA

rm $DB_TESTE

