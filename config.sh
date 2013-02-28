#!/bin/sh

#----------------------------------------------
# The version to build
#----------------------------------------------
VERSION=devel


#----------------------------------------------
# Select the target architecture of the thinjvm
#----------------------------------------------
ARCH_NATIVE=0
ARCH_ARM=1

export ARCH=$ARCH_NATIVE
# export ARCH=$ARCH_ARM


#
# Setup miscellaneous parameters for build:
#
if [ "$ARCH" = "$ARCH_NATIVE" ]; then
    export CMD_PREFIX=
    export ENV_PREFIX=
    export GCC=gcc
    export CFLAGS="-I. -Wall -c -pg"    
    export NOSTDLIB=    
    export AR=ar
elif [ "$ARCH" = "$ARCH_ARM" ]; then
    export CMD_PREFIX=arm-
    export ENV_PREFIX=ARM_
    export GCC=arm-elf-gcc
    export AR=arm-elf-ar
    export CFLAGS="-I. -Wall -c \
           -mcpu=arm7tdmi -mlittle-endian -gdwarf-2 -std=c99 -march=armv4t -mlittle-endian -fno-builtin"
    export NOSTDLIB=nostdlib.o    
else
    echo "Unsupported architecture: $ARCH"
    exit 1
fi

export TOOL_DIR=${CMD_PREFIX}thinj

export DESTINATION=/tools/$TOOL_DIR/$VERSION
export LIBDIR=$DESTINATION/lib
export BINDIR=$DESTINATION/bin
export INCDIR=$DESTINATION/inc

#
# Create tool directories:
#
mkdir -p $BINDIR || exit 1
mkdir -p $LIBDIR || exit 1
mkdir -p $INCDIR || exit 1
