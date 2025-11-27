# Estonian eID OIDC Provider

This is an OIDC Provider (OpenID Connect authentication server) supporting Estonian eID, Mobile-ID, and Smart-ID authentication methods.

## Features

- OIDC-compliant authentication endpoints
- Integration with Estonian eID, Mobile-ID, and Smart-ID
- Session and code storage in Redis
- JWT token generation (access_token, id_token)
- Configurable trust stores for all authentication methods

## Structure

- `src/main/java/com/example/oidc/` - Main Java source code
  - `controllers/` - REST API endpoints
  - `service/` - Business logic for authentication flows
  - `dto/` - Data transfer objects for API and session data
  - `storage/` - Redis and user/session storage
  - `util/` - Utility classes (random code, personal code parsing, etc.)
  - `config/` - Spring configuration for trust stores, clients, and Jackson
- `src/main/resources/application.yml` - Main configuration file

## Configuration

- Trust stores and CA certificates are configured via `application.yml`
- All file paths are relative to the backend root (e.g. `config/idcard.p12`)
- Redis must be running and accessible

## Running

```sh
mvn clean install
mvn spring-boot:run
```

## Testing

```sh
mvn test
```

## Notes

- Requires Java 17+
- See `pom.xml` for dependencies
- For development, logging is set to DEBUG for most components

## License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.

