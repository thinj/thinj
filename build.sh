#!/bin/sh
DESTINATION=/tools/thinj/devel
LIBDIR=$DESTINATION/lib
BINDIR=$DESTINATION/bin

mkdir -p $DESTINATION || exit 1
mkdir -p $LIBDIR || exit 1
mkdir -p $BINDIR || exit 1



javac -sourcepath src -d bin `find src -name "*.java" -print`  || exit 1

jar cf $LIBDIR/thinj.jar -C bin thinj || exit 1

cp src/scripts/env.sh $DESTINATION

cp src/scripts/thinj  src/scripts/retrace $BINDIR
chmod +x $BINDIR/thinj $BINDIR/retrace

exit 0
