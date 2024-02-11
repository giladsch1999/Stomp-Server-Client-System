#include <string>
#include <iostream>
#include <map>
#include <vector>

int main (int argc, char *argv[]) {


std::string frame = "CONNECTED\n" ;
frame+="accept-version:1.2\n";
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
    std::cout<<frame;
    }