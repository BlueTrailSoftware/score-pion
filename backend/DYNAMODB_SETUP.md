# DynamoDB Local Setup

This guide covers how to set up and manage DynamoDB Local for development.

## Prerequisites

- Docker and Docker Compose installed
- AWS CLI installed (for manual database operations)

## Quick Start

### 1. Start DynamoDB Local

Choose one option based on your setup (see [README.md](README.md) for details):

```bash
# Option A: Database only (for development with ./gradlew bootRun)
docker-compose -f docker-compose.dynamodb.yml up -d

# Option B: Full stack (backend + database in containers)
docker-compose up -d
```

This will start:
- DynamoDB Local on port 8000
- DynamoDB Admin UI on port 8001

### 2. Create Tables

```bash
chmod +x scripts/init-dynamodb-local.sh
./scripts/init-dynamodb-local.sh
```

### 3. Verify Setup

**Check tables:**
```bash
aws dynamodb list-tables \
  --endpoint-url http://localhost:8000 \
  --region us-west-2
```

**View data in browser:** http://localhost:8001

## DynamoDB Local Commands

### List all tables
```bash
aws dynamodb list-tables \
  --endpoint-url http://localhost:8000 \
  --region us-west-2
```

### Describe table
```bash
aws dynamodb describe-table \
  --endpoint-url http://localhost:8000 \
  --region us-west-2 \
  --table-name scorepion-data
```

### Scan table
```bash
aws dynamodb scan \
  --endpoint-url http://localhost:8000 \
  --region us-west-2 \
  --table-name scorepion-data
```

### Query by PK
```bash
aws dynamodb query \
  --endpoint-url http://localhost:8000 \
  --region us-west-2 \
  --table-name scorepion-data \
  --key-condition-expression "PK = :pk" \
  --expression-attribute-values '{":pk": {"S": "USER#test-123"}}'
```

### Query GSI1
```bash
aws dynamodb query \
  --endpoint-url http://localhost:8000 \
  --region us-west-2 \
  --table-name scorepion-data \
  --index-name GSI1 \
  --key-condition-expression "GSI1PK = :email" \
  --expression-attribute-values '{":email": {"S": "EMAIL#test@example.com"}}'
```

### Delete table
```bash
aws dynamodb delete-table \
  --endpoint-url http://localhost:8000 \
  --region us-west-2 \
  --table-name scorepion-data
```

## Stop DynamoDB Local

```bash
# Stop containers based on how you started them
docker-compose -f docker-compose.dynamodb.yml down  # if you used Option A
docker-compose down                                  # if you used Option B
```

## Data Persistence

**IMPORTANT:** DynamoDB Local uses `-inMemory` mode by default:
- ✅ No file permission issues
- ✅ Fast performance
- ⚠️ **Data is NOT persisted** - all data is lost when containers stop

### Reset/Recreate Tables

```bash
# Restart containers and recreate tables
docker-compose down && docker-compose up -d
./scripts/init-dynamodb-local.sh
```

### Enable Persistent Mode (Optional)

To keep data between restarts, modify the `command` in your docker-compose file:

```yaml
# Change this:
command: "-jar DynamoDBLocal.jar -sharedDb -inMemory"

# To this:
command: "-jar DynamoDBLocal.jar -sharedDb -dbPath /data"
volumes:
  - ./dynamodb-data:/data
```

Then restart and recreate tables:
```bash
docker-compose down && docker-compose up -d
./scripts/init-dynamodb-local.sh
```

## Application Configuration

The application is already configured to connect to DynamoDB Local in `application.properties`:

```properties
aws.region=us-west-2
aws.dynamodb.endpoint=http://localhost:8000
aws.access-key-id=dummy
aws.secret-access-key=dummy
dynamodb.table.name=scorepion-data
```

**Note:** These settings are already in the project. You don't need to add them unless you're setting up a new environment.
