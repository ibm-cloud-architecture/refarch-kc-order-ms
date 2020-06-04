#!/bin/bash
if [ $# -eq 1 ]
then
  hostn=$1
else
  hostn="localhost:11080"
fi
url="http://$hostn/orders/byManuf/GoodManuf"

echo $(curl  $url)
