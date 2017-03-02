#!/bin/bash

cd ~/clojure/gotit-no-frills
lein clean
lein cljsbuild once min
rsync -av resources/public/ gmp26@nrich.maths.org:/www/nrich/html/gotit
