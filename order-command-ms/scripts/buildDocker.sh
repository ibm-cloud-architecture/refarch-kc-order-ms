
echo "##########################################"
echo " Build ORDER command war and docker image  "
echo "##########################################"
set p = $(echo $PWD | awk -v h="scripts" '$0 ~h')
if [[ $PWD = */scripts ]]; then
 cd ..
fi
. ./scripts/setenv.sh
# When deploying to a cluster with CA certificate we need to install those
# certificates in java keystore. So first transform into der format 
if [ -f ../../refarch-kc/certs/es-cert.pem ] 
then
   openssl x509 -in ../../refarch-kc/certs/es-cert.pem -inform pem -out es-cert.der -outform der
fi

find target -iname "*SNAPSHOT*" -print | xargs rm -rf
rm -rf target/liberty/wlp/usr/servers/defaultServer/apps/expanded
mvn install -DskipITs

# image for public docker hub
docker build -t ibmcase/kc-ordercmdms .
# image for private registry in IBM Cloud
docker tag ibmcase/kc-ordercmdms registry.ng.bluemix.net/ibmcaseeda/kc-ordercmdms