#!/bin/bash

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
  fname='orderCreate.json'
fi
url="http://$hostn/orders"
fn="@./$fname"
echo $fn

# curl -v -H "accept: */*" -H "Content-Type: application/json" -d @./orderCreate.json $url
curl -v -H "accept: */*" -H "Content-Type: application/json" -d $fn $url