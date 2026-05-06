#!/usr/bin/env bash

set -euo pipefail

# Sample Usage
# bash ./add-admin-user.sh yourname@example.com
# ENV=local bash ./add-admin-user.sh yourname@example.com

# Check if email argument is provided
if [ -z "${1:-}" ]; then
    echo "Error: Please provide a user email as the first argument"
    echo "Usage: $0 <user-email>"
    exit 1
fi

if [ "$ENV" = "local" ]; then
    ENDPOINT="--endpoint-url http://localhost:8000"
    echo "Using endpoint for DynamoDB: ${ENDPOINT}"
fi

USER_EMAIL="${1}"
UUID=$(uuidgen)
TABLE_NAME="scorepion-data"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Create user.json
cat <<EOF > user.json
{
  "id": "${UUID}",
  "email": "${USER_EMAIL}",
  "name": "Admin User",
  "googleId": null,
  "pictureUrl": null,
  "role": "ADMIN",
  "isActive": true,
  "createdAt": "${TIMESTAMP}",
  "updatedAt": "${TIMESTAMP}"
}
EOF

# Create role.json
cat <<EOF > role.json
{
  "userId": "${UUID}",
  "role": "ADMIN"
}
EOF

# Create user-item.json with properly escaped data field
cat <<EOF > user-item.json
{
  "PK": {"S": "USER#${UUID}"},
  "SK": {"S": "PROFILE"},
  "Type": {"S": "USER"},
  "GSI1PK": {"S": "EMAIL#${USER_EMAIL}"},
  "GSI1SK": {"S": "USER"},
  "GSI2PK": {"S": "GOOGLEID#null"},
  "GSI2SK": {"S": "USER"},
  "data": {"S": $(jq -c . user.json | jq -R .)},
  "createdAt": {"S": "${TIMESTAMP}"},
  "updatedAt": {"S": "${TIMESTAMP}"}
}
EOF

# Create role-item.json with properly escaped data field
cat <<EOF > role-item.json
{
  "PK": {"S": "ROLE#ADMIN"},
  "SK": {"S": "USER#${UUID}"},
  "Type": {"S": "USER_ROLE_INDEX"},
  "GSI1PK": {"S": "ROLE#ADMIN"},
  "GSI1SK": {"S": "USER#${UUID}"},
  "data": {"S": $(jq -c . role.json | jq -R .)},
  "createdAt": {"S": "${TIMESTAMP}"},
  "updatedAt": {"S": "${TIMESTAMP}"}
}
EOF

# Insert items into DynamoDB
echo "Inserting user item..."
aws dynamodb put-item "${ENDPOINT}" \
    --table-name "${TABLE_NAME}" \
    --item file://user-item.json \
    --region us-east-2

echo "Inserting role item..."
aws dynamodb put-item "${ENDPOINT}" \
    --table-name "${TABLE_NAME}" \
    --item file://role-item.json \
    --region us-east-2

echo "Successfully created admin user with ID: ${UUID}"

# Clean up temporary files
rm -f user.json role.json user-item.json role-item.json