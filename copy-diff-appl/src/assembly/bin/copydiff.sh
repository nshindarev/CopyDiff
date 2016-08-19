#!/bin/bash

main_class=nshindarev.copydiff.appl.Main

copydiff_home=$( cd "$(dirname "${BASH_SOURCE}")" ; cd .. ; pwd -P )

if [ ! -d "$copydiff_home/log" ]; then
    mkdir $copydiff_home/log >/dev/null 2>/dev/null
fi
java -cp "$copydiff_home/conf/:$copydiff_home/lib/*" $main_class *
