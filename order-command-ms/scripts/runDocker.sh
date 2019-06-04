
if [[ $PWD = */scripts ]]; then
 cd ..
fi
if [[ $# -eq 0 ]];then
  kcenv="LOCAL"
else
  kcenv=$1
fi

msname="ordercmdms"
ns="browncompute"
chart=$(ls ./chart/| grep $msname)
kname="kc-"$chart
source ../../refarch-kc/scripts/setenv.sh $kcenv
echo $kname
docker rm $kname
# docker run  --name $kname -e POSTGRESQL_URL=$POSTGRESQL_URL -e  POSTGRESQL_CA_PEM=$POSTGRESQL_CA_PEM -e POSTGRESQL_USER=$POSTGRESQL_USER -e POSTGRESQL_PWD=$POSTGRESQL_PWD  -p 8080:8080 ibmcase/$kname

docker run --name $kname \
--network docker_default \
  -e KAFKA_ENV=$KAFKA_ENV \
  -e KAFKA_BROKERS=$KAFKA_BROKERS \
  -e KAFKA_APIKEY=$KAFKA_APIKEY \
  -e TRUSTSTORE_PWD=${TRUSTSTORE_PWD} \
    -p 10080:9080 -p 10443:9443 -ti  ibmcase/$kname 