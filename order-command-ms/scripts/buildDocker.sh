rm -rf target/liberty/wlp/usr/servers/defaultServer/apps/expanded
mvn install -DskipITs
docker build -t ibmcase/kc-ordercmdms .