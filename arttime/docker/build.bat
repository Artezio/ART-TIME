docker build --tag=arttime-demo/webapp ./webapp/
docker build --tag=arttime-demo/keycloak ./keycloak/
docker build --tag=arttime-demo/database ./database

docker-compose up -d
