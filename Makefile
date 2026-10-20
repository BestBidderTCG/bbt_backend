# Posible Makefile
build:
	./gradlew build

test:
	./gradlew test

run:
	./gradlew bootRun

postgres:
	docker compose up -d

stop-db:
	docker compose down

# Tareas de base de datos
reset-db: postgres
	docker exec -i $(DB_CONTAINER) psql "$(DB_URL)" -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
	make apply-schema