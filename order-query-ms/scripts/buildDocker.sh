echo " ===== Build ORDER Query war and docker image  ====="
set p = $(echo $PWD | awk -v h="scripts" '$0 ~h')
if [[ $PWD = */scripts ]]; then
 cd ..
fi
. ./scripts/setenv.sh
if [ -f ../../refarch-kc/certs/es-cert.pem ] 
then
   openssl x509 -in ../../refarch-kc/certs/es-cert.pem -inform pem -out es-cert.der -outform der
fi

find target -iname "*SNAPSHOT*" -print | xargs rm -rf
rm -rf target/liberty/wlp/usr/servers/defaultServer/apps/expanded
mvn install -DskipITs
# image for public docker hub
docker build -t ibmcase/kc-orderqueryms .
# image for private registry in IBM Cloud
docker tag ibmcase/kc-orderqueryms registry.ng.bluemix.net/ibmcaseeda/kc-orderqueryms
