# Spring Boot Assessment

## Run the Docker command to start the application
```bash
docker run --name maybank-mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=maybankdb -e MYSQL_USER=maybank -e MYSQL_PASSWORD=maybank123 -v mysql_data:/var/lib/mysql -d mysql:8
```

## Run the Docker command to access MySQL
```bash
docker exec -it maybank-mysql mysql -u maybank -pmaybank123 maybankdb
```