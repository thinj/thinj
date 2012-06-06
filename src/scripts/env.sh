#
# This script shall be sourced having its own path as argument, e.g.
# source /opt/thinj/env.sh /opt/thinj
#

export THINJ_HOME=`cd $1; pwd`
export PATH=$THINJ_HOME/bin:$PATH
