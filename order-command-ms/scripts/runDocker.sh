export KAFKA_BROKERS="$(ipconfig getifaddr en0):9092"
export KAFKA_ENV="LOCAL"
docker rm kc-ordercmdms
docker run --name kc-ordercmdms -e KAFKA_BROKERS -e KAFKA_ENV -p 10080:9080 -p 10443:9443 ibmcase/kc-ordercmdms 
