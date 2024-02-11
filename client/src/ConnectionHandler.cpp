#include "../include/ConnectionHandler.h"

using boost::asio::ip::tcp;
using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

ConnectionHandler::ConnectionHandler(std::string host, short port) : host_(host), port_(port), io_service_(),
                                                                socket_(io_service_),isConnected(),subId(1),receiptId(1),
																games(),username(""),receiptlogout(-1),joinreciept(),exitreciept(),m(),shouldterminate(false){}

ConnectionHandler::~ConnectionHandler() {
	close();
}
bool ConnectionHandler::shouldTerminate()
{
	return shouldterminate;
}
bool ConnectionHandler::isconnected()
{
	return isConnected;
				
}
bool ConnectionHandler::setconnected(bool i){
	isConnected=i;
}




 bool ConnectionHandler::containsWord(const std::string& str, const std::string& word){
  		if (str.find(word) != std::string::npos) {
   			 return true;
 		}
  			return false;
		}
	
	void ConnectionHandler:: processFrame(std::string frame){
		std::vector<std::string> parsedFrame;
        std::string delimiter = "\n";
		size_t position=0;
		std::string token = " ";
         while ((position = frame.find(delimiter)) != std::string::npos) {
        token = frame.substr(0, position);
        parsedFrame.push_back(token);
        frame.erase(0, position + delimiter.length());
    }
    parsedFrame.push_back(frame);
	std::string frameWord=parsedFrame[0];
		if(frameWord=="CONNECTED"){ // recieved CONNECTED frame
			isConnected=true;
			std::cout << "Login successful ";
		} else {
			if(frameWord=="ERROR"){ // recieved ERROR frame
				isConnected=false;
				username="";
				games.clear();
				joinreciept.clear();
				exitreciept.clear();
				subId=1;
				receiptId=1;
				receiptlogout=-1; // restarting all clients fields when ERROR received
				std::string str = "passcode";
				if(containsWord(frame,str)){ // wrong passcode 
							std::cout << "Wrong password ";
					} else{ if(containsWord(frame,"logged")){ // User already logged in
								std::cout << "User already logged in ";
								} 
								}
								socket_.close();
								} else{ if(frameWord=="RECEIPT"){ // recieved RECEIPT frame
								           size_t pos=	parsedFrame[1].find(":");
										   std::string recieptIdStr= parsedFrame[1].substr(pos+1,parsedFrame[1].size());
										   int recivedreceiptId= std::stoi(recieptIdStr);
											if(recivedreceiptId==receiptlogout){ // logout frame sent
												username="";
												isConnected=false;
												shouldterminate=true;
												games.clear();
												joinreciept.clear();
												exitreciept.clear();
												subId=1;
												receiptId=1;
												receiptlogout=-1;
												 socket_.close();
												} else{ if(joinreciept.count(recivedreceiptId)>0){ // reciept came from login command
														std::string game=joinreciept[recivedreceiptId];
														std::cout << "Joined channel "+game;	
												} else{ //reciept came from exit command
													std::string game=exitreciept[recivedreceiptId];
														std::cout << "Exited channel "+game;		
												}						
												} 	
																}else{if(frameWord=="MESSAGE"){ // message frame
																	size_t messagepos=	parsedFrame[3].find(":");
																	std::string game=parsedFrame[3].substr(messagepos+1,parsedFrame[3].length());
																	messagepos=	parsedFrame[6].find(":");
																	games.at(game).setA(parsedFrame[6].substr(messagepos+1,parsedFrame[6].length()));
																	messagepos=	parsedFrame[6].find(":");
																	games.at(game).setB(parsedFrame[7].substr(messagepos+1,parsedFrame[6].length()));
																	messagepos=	parsedFrame[5].find(":");
																	std::string usermess=parsedFrame[5].substr(messagepos+1,parsedFrame[3].length());
																	std::vector<std::string> eventVec;
																	int parSize=parsedFrame.size();
																	for(int i=8;i<parSize;i++){
																	std::cout << parsedFrame[i];
																	eventVec.push_back(parsedFrame[i]);
																}
																	games.at(game).parseToSummary(usermess,eventVec);
																}
																}					
		}
		}
		std::cout << frame << std::endl;
	}
	
		 		 
	

// 	names_and_events& ConnectionHandler::sortByTime(names_and_events &fileEvents){
// 	std::sort(fileEvents.events.begin(), fileEvents.events.end(), [](const Event &e1, const Event &e2) {
//     if (e1.gethalftime() != e2.gethalftime()) {
//       // If the two events have different values for beforeHalftime,
//       // sort based on that field.
//       return e1.gethalftime() < e2.gethalftime();
//     } else {
//       // If the two events have the same value for beforeHalftime,
//       // sort based on the time field.
//       return e1.get_time() < e2.get_time();
//     }
//   });
//     return fileEvents;
//      	}



bool ConnectionHandler::connect() {
	std::cout << "Starting connect to "
	          << host_ << ":" << port_ << std::endl;
	try {
		tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
		boost::system::error_code error;
		 socket_.connect(endpoint, error);
		if (error){
			throw boost::system::system_error(error);
		}
	}
	catch (std::exception &e) {
		std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
		return false;
	}

	isConnected=true;

	return true;
}

bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
	size_t tmp = 0;
	boost::system::error_code error;
	try {
		while (!error && bytesToRead > tmp) {
			tmp +=  socket_.read_some(boost::asio::buffer(bytes + tmp, bytesToRead - tmp), error);
		}
		if (error)
			throw boost::system::system_error(error);
	} catch (std::exception &e) {
		std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
		return false;
	}
	return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
	int tmp = 0;
	boost::system::error_code error;
	try {
		while (!error && bytesToWrite > tmp) {
			tmp +=  socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
		}
		if (error)
			throw boost::system::system_error(error);
	} catch (std::exception &e) {
		std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
		return false;
	}
	return true;
}

bool ConnectionHandler::getLine(std::string &line) {
	return getFrameAscii(line, '\n');
}

bool ConnectionHandler::sendLine(std::string &line) {
	std::lock_guard<std::mutex> lock(m);
	return sendFrameAscii(line, '\n');
}


bool ConnectionHandler::getFrameAscii(std::string &frame, char delimiter) {
	char ch;
	// Stop when we encounter the null character.
	// Notice that the null character is not appended to the frame string.
	try {
		do {
			if (!getBytes(&ch, 1)) {
				return false;
			}
			if (ch != '\0')
				frame.append(1, ch);
		} while (delimiter != ch);
	} catch (std::exception &e) {
		std::cerr << "recv failed2 (Error: " << e.what() << ')' << std::endl;
		return false;
	}
	return true;
}

bool ConnectionHandler::sendFrameAscii(const std::string &frame,  char delimiter) {

	bool result = sendBytes(frame.c_str(), frame.length());
	if (!result) return false;
	return sendBytes(&delimiter, 1);
}

// Close down the connection properly.
void ConnectionHandler::close() {
	try {
		 socket_.close();
	} catch (...) {
		std::cout << "closing failed: connection already closed" << std::endl;
	}
}
void ConnectionHandler::createFrame(std::string comand){
		std::lock_guard<std::mutex> lock(m);
		std::vector<Event> events;
		const char endFrame='\0';
		std::vector<std::string> parsedcomand;
        std::string delimiter = " ";
		size_t position=0;
		std::string token = " ";
         while ((position = comand.find(delimiter)) != std::string::npos) {
        token = comand.substr(0, position);
        parsedcomand.push_back(token);
        comand.erase(0, position + delimiter.length());
    }
    parsedcomand.push_back(comand);
    std::string comandword = parsedcomand[0];
		if(comandword=="login") {
		 size_t pos = parsedcomand[1].find(":");
        std::string host = parsedcomand[1].substr(0,pos);
        std::string portStr = parsedcomand[1].substr(pos+1);
        short port = std::stoul (portStr,nullptr,0);
		if(!isConnected){
					 std::cerr<<"is connectd is false" << std::endl;

        	this->port_=port;
		 	this->host_=host;
		  if(!connect()){                                         
            std::cerr<<"Could not connect to server" << std::endl;
           }
         std::stringstream s;
            s << "CONNECT\n" <<
            "accept-version:1.2\n" <<
            "host:stomp.cs.bgu.ac.il\n" <<
            "login:"+parsedcomand[2]+"\n" <<
            "passcode:"+parsedcomand[3]+"\n"+"\n"+"\u0000";                            
            std::string frame = s.str();
			username=parsedcomand[2];
            sendFrameAscii(frame,endFrame); 
		}
	   else{
		 std::cerr<<"The client is already logged in, log out before trying again" << std::endl;
	   }		
		}else{ if(comandword=="join"){
          if(!isConnected)
	  std::cerr<<"The client isn't connected yet" << std::endl;
	  else{
		std::string game = parsedcomand[1];           
			subId++;
			receiptId++;                          
            std::stringstream s;
            s << "SUBSCRIBE\n" <<
                "destination:" + game + "\n" <<
                "id:" + std::to_string(subId) + "\n" <<
                "receipt:" + std::to_string(receiptId) + "\n\n";
            std::string frame = s.str();
            sendFrameAscii(frame,endFrame);
			// games.emplace(game,new Game(subId));
			Game* temp=new Game(subId);
			games.insert(std::pair<std::string, Game>(game,*temp));
			joinreciept.emplace(receiptId,game);		
			}
	

		}else{
			if(comandword=="exit"){
				receiptId++;                          
				std::string game = parsedcomand[1];  
				std::stringstream s;
            s << "UNSUBSCRIBE\n" <<
                "id:" + std::to_string(subId) + "\n" <<
                "receipt:" + std::to_string(receiptId) + "\n\n";
            std::string frame = s.str();
			sendFrameAscii(frame,endFrame);
			games.erase(game);
			exitreciept.emplace(receiptId,game);		

			}else{
				if(comandword=="report"){
				 names_and_events file = parseEventsFile(parsedcomand[1]);  
				 int sizeofEvents=file.events.size();
				for (int i=0;i<sizeofEvents;i++){
					std::string time = std::to_string(file.events[i].get_time());
					std::string generalUpd;
				 for (std::map<std::string, std::string>::map::const_iterator it = file.events[i].get_game_updates().begin(); it != file.events[i].get_game_updates().end(); ++it)
				  {
                   generalUpd +=it->first + ":" + it->second + "\n";
                  }
				  	std::string value;
				    std::string search_string = "before halftime: ";
                    std::size_t search_string_pos = generalUpd.find(search_string);
                    if (search_string_pos != std::string::npos)
                      {
                         std::size_t value_start_pos = search_string_pos + search_string.length();
                          std::size_t value_end_pos = generalUpd.find('\n', value_start_pos);
                           value = generalUpd.substr(value_start_pos, value_end_pos - value_start_pos);
						   file.events[i].sethalftime(value);
					  }
				  std::string teamAUpd;
                for (std::map<std::string, std::string>::map::const_iterator it = file.events[i].get_team_a_updates().begin(); it != file.events[i].get_team_a_updates().end(); ++it) 
				{
                  teamAUpd += it->first+ ":" + it->second + "\n";
				}
				  std::string teamBUpd;
				 for (std::map<std::string, std::string>::map::const_iterator it = file.events[i].get_team_b_updates().begin(); it != file.events[i].get_team_b_updates().end(); ++it) 
				 	{
                   		teamBUpd +=it->first+ ":" + it->second + "\n";
                	}

				std::stringstream s;
				s << "SEND\n" <<
				"destination:"+file.team_a_name+"_"+file.team_b_name+"\n"<<
                "\n" <<
				"user:" + username + "\n"<<
				"team a:"+file.team_a_name+"\n"<<
				"team b:"+file.team_b_name+"\n"<<
				"event name:"+file.events[i].get_name()+"\n"<<
				"time:"+time+"\n"<<
				"general game updates:\n"<<
				generalUpd+"\n"<<
				"team a updates:\n"<<
				teamAUpd+"\n"<<
				"team b updates:\n"<<
                teamBUpd+"\n"<<
				"description\n"+file.events[i].get_discription()+"\n";
				std::string frame = s.str();
				sendFrameAscii(frame,endFrame);
				}
				}else{
				if(comandword=="logout"){
				std::stringstream s;
				s << "DISCONNECT\n" <<
				"receipt:"+std::to_string(receiptId)+"\n"<<
                "\n" <<
				"\n" ;
								receiptlogout=receiptId;
				std::string frame = s.str();
				sendFrameAscii(frame,endFrame);
				}
				else
				if(comandword=="summary"){
					std::string toWrite="";
					std::string game=parsedcomand[1];
					std::string usertowrite=parsedcomand[2];			
					toWrite=games.at(game).getA()+"vs"+games.at(game).getB()+"\n"
					+"Games stats:"+"\n"+
					games.at(game).getDesByUser(usertowrite)+"\n"
					+"Game event reports:"+"\n";
					int temp=games.at(game).events_by_user[usertowrite].size();
					for(int i=0;i<temp;i++){
							toWrite=toWrite+games.at(game).events_by_user[usertowrite][i]+"\n"
							+"\n";
					}
					   std::ofstream file(parsedcomand[3]);
  						  if (file.good()) {
      					  file << toWrite << std::endl;
						
				}
				else
				std::cerr<<" not a valid command" << std::endl;
				}
				

				 
				 

				}
			}
		}
		
	}
}
     
