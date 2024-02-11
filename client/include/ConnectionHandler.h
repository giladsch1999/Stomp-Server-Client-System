#pragma once
// #include <string>
#include <iostream>
#include <fstream>
#include <boost/asio.hpp>
#include "../src/Game.cpp"
// #include <map>
#include "../include/event.h"



// using boost::asio::ip::tcp;

class ConnectionHandler {
private:
      std::mutex m;
	 std::string host_;
	 short port_;
	boost::asio::io_service io_service_;   // Provides core I/O functionality
	boost::asio::ip::tcp::socket socket_;
	 bool isConnected;
	 int subId;
	 int receiptId;
	 std::map<std::string,Game> games;
	 std::string username;
	 int receiptlogout;
	 std::map<int,std::string> joinreciept;
	 std::map<int,std::string> exitreciept;
     bool shouldterminate;
	 bool containsWord(const std::string& str, const std::string& word);
	 
public:
	ConnectionHandler(std::string host, short port);

	virtual ~ConnectionHandler();
	bool shouldTerminate();
	bool isconnected();
	bool setconnected(bool i);



	void processFrame(std::string  frame);

	// Connect to the remote machine
	bool connect();

	// Read a fixed number of bytes from the server - blocking.
	// Returns false in case the connection is closed before bytesToRead bytes can be read.
	bool getBytes(char bytes[], unsigned int bytesToRead);

	// Send a fixed number of bytes from the client - blocking.
	// Returns false in case the connection is closed before all the data is sent.
	bool sendBytes(const char bytes[], int bytesToWrite);

	// Read an ascii line from the server
	// Returns false in case connection closed before a newline can be read.
	bool getLine(std::string &line);

	// Send an ascii line from the server
	// Returns false in case connection closed before all the data is sent.
	bool sendLine(std::string &line);

	// Get Ascii data from the server until the delimiter character
	// Returns false in case connection closed before null can be read.
	bool getFrameAscii(std::string &frame, char delimiter);

	// Send a message to the remote host.
	// Returns false in case connection is closed before all the data is sent.
	bool sendFrameAscii(const std::string &frame, char delimiter);

	// Close down the connection properly.
	void close();
	
	void createFrame(std::string comand);

	// names_and_events& sortByTime(names_and_events &events);

	

}; //class ConnectionHandler
