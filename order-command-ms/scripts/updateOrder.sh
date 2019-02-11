#!/bin/bash

if [ $# -eq 2 ]
then
  orderID=$1
  hostn=$2
else
  if [ $# -eq 1 ]
  then
    orderID=$1   
  else
    echo "Usage $0 orderID [hostname]"
  fi
  hostn="localhost:10080"
fi
url="http://$hostn/orders/$orderID"

curl -X PUT -H "Content-Type: application/json" -d "@./orderUpdate.json" $url
