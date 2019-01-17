export KAFKA_BROKERS="kafka1:9092"
export KAFKA_ENV="LOCAL"
docker rm kc-orderqueryms
docker run --name kc-orderqueryms -e KAFKA_BROKERS -e KAFKA_ENV -p 11080:9080 -p 11443:9443 ibmcase/kc-orderqueryms 
