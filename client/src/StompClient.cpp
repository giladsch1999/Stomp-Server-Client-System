#include <stdlib.h>
#include "../include/ConnectionHandler.h"
#include <thread>

bool shouldterminate=false;


void keyBoardRun(ConnectionHandler* CH){
     while (1) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
		std::string line(buf);
		int len=line.length();
       CH->createFrame(line);
	}
}
void socketReaderRun(ConnectionHandler* CH)//login 127.0.0.1:7777 as ab    join Germany_Japan    report ../data/events1_partial.json
        {            
            // std::cout << "po successful 1 ";
            				
while(true){
    while (CH->isconnected())
    {                        				
// std::cout << "po successful 2 ";
        std::string input="";
        if(CH->getFrameAscii(input,'\0'))
        {
            CH->processFrame(input);   
            			std::cout << input<<std::endl;
                        				// std::cout << "po successful ";

               
        }
            // input = "";
    }
}
}

int main(int argc, char *argv[]) {
    do{
    std::string host ="0" ;
    short port = 0;
    ConnectionHandler* connectionhandler= new ConnectionHandler(host, port);
    std::thread keyboardIS_thread(keyBoardRun, connectionhandler);
    std::thread socketReader_thread(socketReaderRun, connectionhandler);
    keyboardIS_thread.join();
	socketReader_thread.join(); 
    }while(1);

	return 0;
}
