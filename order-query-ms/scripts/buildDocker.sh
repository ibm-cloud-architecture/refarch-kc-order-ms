
echo "##########################################"
echo " Build ORDER Query war and docker image  "
echo "##########################################"

if [[ $PWD = */scripts ]]; then
 cd ..
fi
if [[ $# -eq 0 ]];then
  kcenv="local"
else
  kcenv=$1
fi

. ./scripts/setenv.sh
if [[ $kcenv -ne "local" && -f ../../refarch-kc/certs/es-cert.pem ]] 
then
   openssl x509 -in ../../refarch-kc/certs/es-cert.pem -inform pem -out es-cert.der -outform der
fi

find target -iname "*SNAPSHOT*" -print | xargs rm -rf
rm -rf target/liberty/wlp/usr/servers/defaultServer/apps/expanded
tools=$(docker images | grep javatools)
if [[ -z "$tools" ]]
then
  mvn install -DskipITs
else
   docker run -v $(pwd):/home -ti ibmcase/javatools bash -c "cd /home && mvn install -DskipITs"
fi
# image for public docker hub
docker build -t ibmcase/$kname .
if [[ $kcenv != "local" ]]
then
   # image for private registry in IBM Cloud
   docker tag ibmcase/$kname us.icr.io/ibmcaseeda/$kname
fi
