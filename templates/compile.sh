#!/bin/bash
PASTAJAR=pasta-junit.jar
LIBLOC=../war/WEB-INF/template_content/lib

if test -e $LIBLOC/$PASTAJAR ; then
    rm $LIBLOC/$PASTAJAR
fi

javac pasta/*.java -classpath .:$LIBLOC/junit-4.12.jar
if [[ $? -ne 0 ]]; then
    exit 1;
fi

jar cf $PASTAJAR pasta/*.class
mv $PASTAJAR $LIBLOC
if [[ $? -eq 0 ]]; then
    echo Created $PASTAJAR in `realpath $LIBLOC`
else
    echo Created $PASTAJAR
fi
rm -f pasta/*.class
