# Chat API

A RESTful chat application built with Quarkus, Java 21, and PostgreSQL, leveraging Hibernate Reactive Panache for database interactions and SmallRye JWT for authentication. This project supports user registration, login, and messaging (broadcast, direct, and group) with a reactive, non-blocking architecture.

## Features
- **User Registration**: Create users with unique usernames and hashed passwords.
- **User Login**: Authenticate users and receive a JWT token.
- **Messaging**:
  - **Broadcast**: Send messages to all users.
  - **Direct**: Send messages to a specific user.
  - **Group**: Send messages to a group identified by a group ID.
- **Message Retrieval**: Fetch all messages (authenticated access only).
- **Security**: JWT-based authentication with role-based access control (`user` role).

## Prerequisites
To run this project locally, ensure you have the following installed:
- **Java 21+**: OpenJDK or similar (e.g., `sudo apt install openjdk-21-jdk` on Ubuntu).
- **Maven**: Included via `mvnw` in the repo, no separate installation needed.
- **PostgreSQL**: Version 14+ (e.g., `sudo apt install postgresql` on Ubuntu).
- **Git**: For cloning the repo (e.g., `sudo apt install git`).
- **jq**: Optional, for parsing JSON in scripts (e.g., `sudo apt install jq`).

## Project Setup
Follow these steps to set up and run the Chat API locally.

### 1. Clone the Repository
```bash
git clone https://github.com/Levon001/chat_api.git
cd chat_api
```

### 2. Configure PostgreSQL
1. **Start PostgreSQL**:
   - Run the following command to start the PostgreSQL service:
     ```bash
     sudo systemctl start postgresql 
```
   - Verify it’s running:
    ```bash
    sudo systemctl status postgresql 
```
2. **Create Database and User**:
   - Log in to PostgreSQL as the postgres user:
   ```bash
   psql -U postgres 
   ```

   - Execute these SQL commands to set up the database and user:

   ```sql
   CREATE DATABASE chat_db;
   CREATE USER chat_user WITH PASSWORD 'your_secure_password';
   GRANT ALL PRIVILEGES ON DATABASE chat_db TO chat_user;
   \q 
   ```
  - Replace your_secure_password with a strong password of your choice.

3. **Set Up Tables (Optional)**:
   - Quarkus auto-generates tables due to quarkus.hibernate-orm.database.generation=update, but to enforce username uniqueness:
   ```bash
   psql -h localhost -U chat_user -d chat_db ```
  ```sql
  ALTER TABLE chat_user ADD CONSTRAINT unique_username UNIQUE (username);
  CREATE SEQUENCE message_seq OWNED BY message.id;
  ALTER TABLE message ALTER COLUMN id SET DEFAULT nextval('message_seq');
  \q 
```

### 3. Configure Environment Variables
-The application.properties file uses placeholders for sensitive or configurable settings. Set these environment variables locally:
```bash
export DB_USERNAME=chat_user               # Database username (default: chat_user)
export CHAT_PASSWORD=your_secure_password  # Database password (required)
export DB_URL=vertx-reactive:postgresql://localhost:5432/chat_db  # Database URL (default)
export JWT_PRIVATE_KEY_PATH=/path/to/privateKey.pem  # Path to your private key
```
Username Configuration: The default username is chat_user, but you can override it with DB_USERNAME (e.g., export DB_USERNAME=other_user) to adapt to different database setups without modifying application.properties.
Password: Replace your_secure_password with the password set in Step 2.
URL: Default is local PostgreSQL; override with DB_URL for remote servers.
JWT Key: Replace /path/to/privateKey.pem with the actual location of your private key file.

### 4. Generate JWT Keys
-The repo includes publicKey.pem, but you need to generate a matching privateKey.pem locally (do not commit it):

```bash
openssl genrsa -out privateKey.pem 2048
openssl rsa -in privateKey.pem -pubout -out publicKey.pem
```
- Move the Private Key: Place privateKey.pem in a secure location (e.g., ~/chat_api/privateKey.pem) and update JWT_PRIVATE_KEY_PATH accordingly.
- Verify Public Key: Ensure src/main/resources/META-INF/resources/publicKey.pem matches your private key (overwrite it if you regenerate).

### 5. Build and Run
- Start the application in development mode:
```bash
./mvnw quarkus:dev 
```
- The API will be available at http://localhost:8080.


## Usage
Use `curl` or any HTTP client to interact with the API endpoints. The examples below assume `jq` is installed for parsing JSON responses.

### Register a User
```bash
curl -X POST -H "Content-Type: application/json" \
     -d '{"username":"user1","password":"pass123"}' \
     http://localhost:8080/api/chat/register
```
## Login
```bash
TOKEN=$(curl -s -X POST -H "Content-Type: application/json" \
     -d '{"username":"user1","password":"pass123"}' \
     http://localhost:8080/api/chat/login | jq -r '.token')
echo "Token: $TOKEN"
```
## Send a Broadcast Message
```bash
curl -X POST -H "Content-Type: application/json" \
     -H "Authorization: Bearer $TOKEN" \
     -d '{"sender":"user1","content":"Hello everyone"}' \
     http://localhost:8080/api/chat/broadcast
```
## Send a Direct Message
```bash
curl -X POST -H "Content-Type: application/json" \
     -H "Authorization: Bearer $TOKEN" \
     -d '{"sender":"user1","recipient":"user2","content":"Hi user2!"}' \
     http://localhost:8080/api/chat/direct
```
## Send a Group Message
```bash
curl -X POST -H "Content-Type: application/json" \
     -H "Authorization: Bearer $TOKEN" \
     -d '{"sender":"user1","groupId":"group1","content":"Hello group1"}' \
     http://localhost:8080/api/chat/group
```
## Retrieve All Messages
```bash
curl -X GET -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/chat/messages
- Response: JSON array of messages, e.g.: 
```
```json
[
  {"id":1,"sender":"user1","recipient":null,"groupId":null,"content":"Hello everyone","timestamp":1741014731898},
  {"id":2,"sender":"user1","recipient":"user2","groupId":null,"content":"Hi user2!","timestamp":1741015150720}
]
```
