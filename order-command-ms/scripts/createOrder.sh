#!/bin/bash

if [[ $PWD = */scripts ]]; then
 cd ..
fi

if [ $# -eq 2 ]
then
  hostn=$1
  fname=$2
else
  if [ $# -eq 1 ]
  then
    hostn=$1   
  else
    hostn="localhost:10080"
  fi
  fname='./scripts/orderCreate.json'
fi

url="http://$hostn/orders"

# curl -v -H "accept: */*" -H "Content-Type: application/json" -d @./orderCreate.json $url
curl -v -H "accept: */*" -H "Content-Type: application/json" -d $fname $url