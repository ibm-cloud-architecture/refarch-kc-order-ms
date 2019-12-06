#!/bin/bash

if [[ $PWD = */scripts ]]; then
 cd ..
fi

if [ $# -eq 3 ]
then
  hostn=$1
  fname=$2
  count=$3
fi

if [ $# -eq 2 ]
then
    hostn=$1
    fname=$2
    count=10
fi

if [ $# -eq 1 ]
then
    hostn="localhost:10080"
    fname=$PWD/scripts/orderCreate.json
    count=10
fi

url="http://$hostn/orders"

for ORDER in $(seq 0 $count)
do
 echo "$ORDER"
 echo "Send $fname to $url"
 curl -v -X POST  -H "accept: */*" -H "Content-Type: application/json" -d @$fname $url
done
