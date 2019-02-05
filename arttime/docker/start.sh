docker build --tag=arttime-demo/database database/
docker build --tag=arttime-demo/keycloak keycloak/
docker build --tag=arttime-demo/webapp webapp/

docker-compose up -d