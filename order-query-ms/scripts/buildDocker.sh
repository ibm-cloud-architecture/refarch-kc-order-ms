
echo "##########################################"
echo " Build ORDER Query war and docker image  "
echo "##########################################"

msname="orderqueryms"
kname="kc-orderqueryms"
ns="greencompute"


if [[ $PWD = */scripts ]]; then
 cd ..
fi
if [[ $# -eq 0 ]];then
  kcenv="LOCAL"
else
  kcenv=$1
fi

source ../../refarch-kc/scripts/setenv.sh $kcenv


find target -iname "*SNAPSHOT*" -print | xargs rm -rf
rm -rf target/liberty/wlp/usr/servers/defaultServer/apps/expanded
tools=$(docker images | grep javatools)
if [[ -z "$tools" ]]
then
  mvn install -DskipITs
else
   docker run -v $(pwd):/home -ti ibmcase/javatools bash -c "cd /home && mvn install -DskipITs"
fi


if [[ "$kcenv" = "LOCAL" ]]
then
   # image for public docker hub or local repo - no CA certificate
   echo "Build docker image for $kname local run"
   docker build --build-arg envkc=$kcenv -t ibmcase/$kname .
else

      # image for private registry in IBM Cloud
   echo "Build docker image for $kname to deploy on $kcenv"

  docker build \
      --build-arg KAFKA_ENV=$kcenv \
      --build-arg KAFKA_BROKERS=${KAFKA_BROKERS} \
      --build-arg KAFKA_APIKEY=${KAFKA_APIKEY} \
      --build-arg JKS_LOCATION=${JKS_LOCATION} \
      --build-arg TRUSTSTORE_PWD=${TRUSTSTORE_PWD} \
      -t us.icr.io/ibmcaseeda/$kname .
fi