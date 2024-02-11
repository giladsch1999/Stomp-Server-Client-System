# Stomp-Server-Client-System
 
## STOMP Server and Client in SPL

This repository contains the implementation of a STOMP (Streaming Text Oriented Messaging Protocol) server and client in SPL (Systems Programming Language) course at the university. The aim of this project is to provide a simple messaging system that allows clients to communicate with each other through the server. The server is fully implemented in Java and uses the TCP and Reactor design patterns.

**Features:**
- Support for sending and receiving messages between clients through the server.
- Support for multiple clients connecting to the server at the same time.
- Implementation of basic STOMP protocol commands: CONNECT, SEND, SUBSCRIBE, UNSUBSCRIBE, and DISCONNECT.

**Requirements:**
- A C++ compiler that supports C++11.
- Boost library for networking and threading.
- Java 8 or higher.

**Getting Started:**
1. Clone the repository to your local machine.
2. **Server Start:**
  - Access folder: `cd server`
  - Build using: `mvn compile`
  - Run the server (choose one design pattern):
    - TPC pattern: `mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="<port> tpc"`
    - Reactor pattern: `mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="<port> reactor"`
3. **Client Start:**
  - Access folder: `cd client`
  - Build using: `make`
  - Run the client: `bin/StompWCIClient`
  
**Client Commands:**
- Login: `login {host:port} {username} {password}`
- Subscribe: `join {channel_name}`
- Unsubscribe: `exit {channel_name}`
- Send: `report {file_path}`
- Summary: `summary {channel_name} {user} {file}`
- Logout: `logout`
