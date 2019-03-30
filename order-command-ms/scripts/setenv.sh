#!/bin/bash

export msname="ordercommandms"
export chart=$(ls ./chart/| grep $msname)
export kname="kc-ordercmdms"
export ns="browncompute"
