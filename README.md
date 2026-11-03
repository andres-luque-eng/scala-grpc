# scala-grpc

Sample gRPC service built with **Scala 3**, **Apache Pekko**, and **Pekko gRPC**.

---

## Prerequisites

- **Java JDK**: 17 or higher (JDK 21 LTS recommended)
- **sbt**: 1.10.x
- **grpcurl**: Command-line tool for interacting with gRPC servers ([installation guide](https://github.com/fullstorydev/grpcurl))

---

## Local Development Setup

To run and customize the server on your local machine without checking personal settings into git:

1. **Copy the local template to `local.conf`**:
   ```bash
   cp src/main/resources/local.conf.template src/main/resources/local.conf
   ```
   > `local.conf` is ignored by `.gitignore` so your personal overrides remain local.

2. **Customize `local.conf` (optional)**:
   You can change the host, port, or logging level inside `src/main/resources/local.conf`:
   ```hocon
   include "application.conf"

   grpc {
     server {
       host = "127.0.0.1"
       port = 3031
     }
   }

   pekko {
     loglevel = "DEBUG"
   }
   ```

---

## Running the Server

### 1. Default (Development Environment)
When started without arguments, the server defaults to the `development` environment (`development.conf`, port `3031`, log level `DEBUG`):
```bash
sbt run
```

### 2. Using Command-line Arguments
You can specify the environment and override the gRPC port using CLI parameters:

```bash
# Run with the local configuration file:
sbt "run --config=local"

# Run with staging configuration:
sbt "run --config=staging"

# Run with staging configuration and override port to 3035:
sbt "run --config=staging --grpc.port=3035"

# Run with production configuration:
sbt "run --config=production"
```

*(You can also use `runMain com.example.grpc.Server ...` if preferred).*

### 3. Using Environment Variables
Deploy-time settings can be injected via standard environment variables:

```bash
# Override the port:
GRPC_PORT=50051 sbt run

# Override the host and port:
GRPC_HOST=127.0.0.1 GRPC_PORT=50051 sbt run
```

---

## Configuration Precedence

The server resolves configuration values using the following precedence order (**highest wins**):

```
CLI arguments (--grpc.port=...)
  └── Environment variables ($GRPC_PORT, $GRPC_HOST, $LOG_LEVEL)
        └── Environment file (local.conf / development.conf / staging.conf / production.conf)
              └── Base file (application.conf)
```

### Supported CLI Arguments
| Argument | Description | Allowed Values | Default |
|---|---|---|---|
| `--config=<env>` | Selects the configuration profile | `local`, `development`, `staging`, `production` | `development` |
| `--grpc.port=<number>` | Overrides the listening gRPC port | `1` to `65535` | From config profile |

If an invalid environment or malformed port is passed, the server aborts immediately with a descriptive error message and exit code `1`.

---

## Verifying with `grpcurl`

Once the server is running, you can test it from another terminal using `grpcurl`:

### 1. List Available Services (Reflection)
```bash
grpcurl -plaintext localhost:3031 list
```
**Expected Output:**
```
com.example.grpc.GreeterService
grpc.reflection.v1alpha.ServerReflection
```

### 2. Inspect the Service Schema
```bash
grpcurl -plaintext localhost:3031 describe com.example.grpc.GreeterService
```

### 3. Invoke the RPC
```bash
grpcurl -plaintext -d '{"name": "Alice"}' localhost:3031 com.example.grpc.GreeterService/SayHello
```
**Expected Output:**
```json
{
  "message": "Hello, Alice! Welcome to Scala 3."
}
```

---

## Running Automated Tests

Run the full automated test suite (unit tests and end-to-end integration tests):

```bash
sbt test
```

### Test Suite Structure
- **Unit Tests (`GreeterServiceImplSpec`)**: Tests service logic in isolation using async futures without network or port overhead.
- **Integration Tests (`GreeterServiceIntegrationSpec`)**: Spawns an in-process Pekko gRPC server on an OS-assigned ephemeral port (`port = 0`) and exercises real RPC calls over HTTP/2 using the generated `GreeterServiceClient`.

