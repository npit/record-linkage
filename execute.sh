#!/usr/bin/env bash
echo "$(find $(pwd) -iname '*.jar' | tr '\n' ':')" > cpath 

java -Xmx12g -XX:-UseGCOverheadLimit  -cp "$(cat cpath)" gr.demokritos.iit.skel.yds.ydsmatcher.YDSMatcher config.properties
