docker build --tag=artezio/arttime-demo-database database/
docker build --tag=artezio/arttime-demo-keycloak keycloak/
docker build --tag=artezio/arttime webapp/

docker-compose up -d