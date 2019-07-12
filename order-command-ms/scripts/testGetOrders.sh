#!/bin/bash
if [ $# -eq 1 ]
then
  hostn=$1
else
  hostn="localhost:10080"
fi
url="http://$hostn/orders"

echo $(curl  $url)
