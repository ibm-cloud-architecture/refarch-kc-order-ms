#!/bin/bash
set p = $(echo $PWD | awk -v h="scripts" '$0 ~h')
if [[ $PWD = */scripts ]]; then
 cd ..
fi
export msname="orderqueryms"
export chart=$(ls ./chart/| grep $msname)
export kname="kc-orderquery"
export ns="browncompute"
