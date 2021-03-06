#!/usr/bin/env bash
conffile=config.properties
[ $# -gt 0 ] && conffile="$1"
echo "$(find $(pwd) -iname '*.jar' | tr '\n' ':')" > cpath 

java -Xmx12g -XX:-UseGCOverheadLimit  -cp "$(cat cpath)" gr.demokritos.iit.skel.yds.ydsmatcher.YDSMatcher "$conffile"
