#!/bin/sh

find . -iname '*.java' |
    xargs grep -Pn "[\x80-\xFF]"

