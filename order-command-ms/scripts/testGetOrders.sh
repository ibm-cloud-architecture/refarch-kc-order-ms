#!/bin/bash
if [ $# -eq 1 ]
then
  hostn=$1
else
  hostn="localhost:9080"
fi
url="http://$hostn/orders"

curl  $url
