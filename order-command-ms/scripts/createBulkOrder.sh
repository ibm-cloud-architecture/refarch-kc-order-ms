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

PRODUCTS=(
 "Apples"
 "Oranges"
 "Carrots"
 "Tomatoes"
 "Kiwis"
 "Pineapples"
)

for ORDER in $(seq 1 $count)
do

 IDX=$RANDOM
 let "IDX %= 6"
 PRODUCT=${PRODUCTS[$IDX]}

 COUNT=$RANDOM
 let "COUNT %= 500"

 cat $fname | jq ".quantity=${COUNT} | .productID=\"${PRODUCT}\"" > tmpfile.json
 
 echo "$ORDER"
 echo "Send $fname to $url"
 #curl -v -H "accept: */*" -H "Content-Type: application/json" -d @$fname $url
 curl -v -H "accept: */*" -H "Content-Type: application/json" -d @tmpfile.json $url

 rm tmpfile.json

done
