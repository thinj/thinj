#!/bin/sh

# Setup environment:
.  ../thinj/config.sh || exit 1


[ -d bin ] && rm -rf bin

mkdir bin

javac -sourcepath src -d bin `find src -name "*.java" -print`  || exit 1

jar cf $LIBDIR/thinj.jar -C bin thinj || exit 1


# Build environment script:
cat src/scripts/env.sh | sed "s/__THINJ_HOME__/${ENV_PREFIX}THINJ_HOME/g" | sed "s#__DESTINATION__#${DESTINATION}#g" > $DESTINATION/env.sh 

cat src/scripts/thinj | sed "s/__THINJ_HOME__/${ENV_PREFIX}THINJ_HOME/g" > $BINDIR/${CMD_PREFIX}thinj
cp src/scripts/retrace $BINDIR/${CMD_PREFIX}retrace

chmod +x $BINDIR/${CMD_PREFIX}thinj $BINDIR/${CMD_PREFIX}retrace

exit 0
