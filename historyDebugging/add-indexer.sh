#!/bin/bash
DB=$1
[ "x$DB" == "x" ] && echo "need database" && exit

sqlite3 $DB 'alter table entries add indexer TEXT;'
sqlite3 $DB 'select id,pagenum,offset from entries;' | awk -F \| '{ printf "update entries set indexer=\"%02d%04d\" where id=%d;\n", $2, $3, $1; }' > make-indexer-commands.txt
sqlite3 $DB -init make-indexer-commands.txt '.quit;'

