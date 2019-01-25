cd webapp
docker build --tag=arttime-demo/webapp .
cd ..
cd database
docker build --tag=arttime-demo/database .
cd ..
docker network create --internal arttime-internal-network
docker-compose up
