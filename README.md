# bbt_backend
Repositorio perteneciente al backend de la aplicación

DISTRIBUCION DE LAS CARPETAS MAS IMPORTANTES
*   **`domain`**: Son las clases de datos tal cual para kotlin. **No tienen dependencias de Spring ni de bases de datos.**
*   **`usecase`**: La lógica de negocio. Aquí se decide qué se hace y las funciones como tal.
*   **`adapter/web`**: Aquí van los **ENDPOINTS** y los **DTOs** (los JSONs de entrada/salida).
*   **`adapter/persistence`**: La conexión real a la base de datos. Aquí van las **Entidades JPA** (que generan el SQL automáticamente) y los **Mappers** que traducen de Entidad a Dominio.

PARA AÑADIR NUEVAS COSAS Y CAMPOS Y DEMAS

Añadir por ejemplo descripcion a un user
1.  **`domain/user/User.kt`**: Añadir el campo a la data class.
2.  **`adapter/persistence/user/UserEntity.kt`**: Añadir el campo con su anotación `@Column`.
3.  **`adapter/persistence/user/UserPersistenceAdapter.kt`**: ACTUALIZAR las funciones `toDomain()` y `toEntity()` para que incluyan el nuevo campo.
4.  **`resources/db/schema.sql`**: (No deberia hacer falta porq se actualiza solo epro por si acaso).




