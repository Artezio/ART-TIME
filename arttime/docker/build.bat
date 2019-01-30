docker build --tag=arttime-demo/webapp ./webapp/
docker build --tag=arttime-demo/keycloak ./keycloak/
docker build --tag=arttime-demo/database ./database

docker network create --internal arttime-internal-network
docker-compose up
