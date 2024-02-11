#include <algorithm>
#include <mutex>
#include <unordered_map>
#include <unordered_set>
#include <vector>
#include <iostream>
class Game
{
private:
    int subId;
    std::unordered_map<std::string, std::string> descriptionByUser{};
    std::string teamA;
    std::string teamB;


public:

    // Maps a user name to a vector of events that they updated.
    std::unordered_map<std::string, std::vector<std::string>> events_by_user{};

    // Set of all user IDs in the game.
    std::unordered_set<std::string> users{};

    // Mutex to protect access to the events_by_user and users data.
    std::mutex m;

    Game(int subI) :subId(subI), teamA(), teamB(), m() {}

    Game(const Game& other) :subId(other.subId), descriptionByUser(), teamA(), teamB(), events_by_user(), users(), m() {

    }
    bool operator==(const Game& game) const {
        return (teamA == game.teamA && teamB == game.teamB);
    }

    std::string& getDesByUser(std::string username) {
        return descriptionByUser.at(username);
    }

    void  setA(std::string i) {
        teamA = i;
    }

    std::string  getA() {
        return teamA;
    }

    std::string  getB() {
        return teamB;
    }

    void setB(std::string i) {
        teamB = i;
    }

    // Adds an event to the list of events for the specified user.
    void  add_event(std::string userName, const std::string& event) {
        std::lock_guard<std::mutex> lock(m);
        if (users.count(userName) > 0) {
            events_by_user[userName].push_back(event);
            users.insert(userName);
        }
    }
    void  parseToSummary(std::string userName, const std::vector<std::string>& event) 
    {
        std::string toEdit = "";
        if (descriptionByUser[userName] == "") {
            toEdit += ("general game updates:\n");
            toEdit += "team a updates:\n";
            toEdit += "team b updates:\n";
            descriptionByUser[userName] = toEdit;
        }
        size_t pos = event[0].find(":");
        std::string eventName = event[0].substr(pos + 1, event[0].size());
        pos = event[1].find(":");
        std::string time = event[1].substr(pos + 1, event[1].size());
        auto generalpos = std::find(event.begin(), event.end(), "general game updates:");
        auto teamApos = std::find(event.begin(), event.end(), "team a updates:");
        auto teamBpos = std::find(event.begin(), event.end(), "team b updates:");
        auto descpos = std::find(event.begin(), event.end(), "description");
        size_t start = descriptionByUser.at(userName).find("general game updates:");
        size_t end = descriptionByUser.at(userName).find("team a updates:");
        std::vector<std::string> updates;
        for (auto it = generalpos + 1; it < teamApos; it++)  //start general des parse
        {
            int i = it - event.begin();
            // std::cout<< i;
            pos = event.at(i).find(":");
            if (pos == std::string::npos)
                continue;

            std::string lineheader = event.at(i).substr(0, pos);
            bool found = false;
            if (descriptionByUser.at(userName).substr(start, end).find(lineheader) != std::string::npos)
                found = true;
            if (found)
            {
                std::cout << "updating stat";
                std::string temp = event[i];
                size_t pos2 = descriptionByUser[userName].find(lineheader);
                size_t end_pos = descriptionByUser.at(userName).find('\n', pos2);
                descriptionByUser.at(userName).replace(pos2, end_pos - pos2, temp);

            }//if its a new stat
            else
            {
                std::cout << "new stat";
                updates.push_back(event[i]);

            }
        }
        std::string ogstring = descriptionByUser[userName];//after the replacemnt
        std::string delimiter = "\n";
        size_t position = descriptionByUser.at(userName).find("general game updates:");
        std::string token = " ";
        size_t start_position = descriptionByUser.at(userName).find(delimiter, start);
        while ((position = descriptionByUser.at(userName).find(delimiter, start_position+1)) != std::string::npos) 
        {
            token = descriptionByUser.at(userName).substr(start_position+1, position- (start_position + delimiter.size()));
            if (token == "team a updates:")
                break;
            updates.push_back(token);
            descriptionByUser.at(userName).erase(start_position, token.length() + delimiter.length());
            start_position = position- (token.length() + delimiter.length());
        }
        std::sort(updates.begin(), updates.end());
        std::string ans = "";
        int size = updates.size();
        for (int i = 0; i < size; i++)
        {
            ans = ans + updates[i] + "\n";
        }
        descriptionByUser.at(userName) = split(ogstring, "general game updates:", "team a updates:", ans);
        updates.clear();// general des parsed
        start = descriptionByUser.at(userName).find("team a updates:"); //start team a des parse
        end = descriptionByUser.at(userName).find("team b updates:");
        for (auto it = teamApos+1; it < teamBpos; it++) {
            int i = it - event.begin();
            // std::cout<< i;
            pos = event.at(i).find(":");
            if (pos == std::string::npos)
                continue;

            std::string lineheader = event.at(i).substr(0, pos);
            bool found = false;
            if (descriptionByUser.at(userName).substr(start, end).find(lineheader) != std::string::npos)
                found = true;
            if (found)
            {
                std::cout << "updating stat";
                std::string temp = event[i];
                size_t pos2 = descriptionByUser[userName].find(lineheader);
                size_t end_pos = descriptionByUser.at(userName).find('\n', pos2);
                descriptionByUser.at(userName).replace(pos2, end_pos - pos2, temp);

            }//if its a new stat
            else
            {
                std::cout << "new stat";
                updates.push_back(event[i]);

            }
        }
        ogstring = descriptionByUser[userName];//after the replacemnt
        delimiter = "\n";
        position = descriptionByUser.at(userName).find("team b updates:");
        token = " ";
        start_position = descriptionByUser.at(userName).find(delimiter, start);
        while ((position = descriptionByUser.at(userName).find(delimiter, start_position + 1)) != std::string::npos)
        {
            token = descriptionByUser.at(userName).substr(start_position + 1, position - (start_position + delimiter.size()));
            if (token == "team b updates:")
                break;
            updates.push_back(token);
            descriptionByUser.at(userName).erase(start_position, token.length() + delimiter.length());
            start_position = position - (token.length() + delimiter.length());
        }
        std::sort(updates.begin(), updates.end());
        ans = "";
        size = updates.size();
        for (int i = 0; i < size; i++) {
            ans = ans + updates[i] + "\n";
        }
        descriptionByUser.at(userName) = split(ogstring, "team a updates:", "team b updates:", ans);
        updates.clear();// team a des parsed
        start = descriptionByUser.at(userName).find("team b updates:"); //start team b des parse
        end = descriptionByUser.at(userName).size();
        for (auto it = teamBpos+1; it < descpos; it++) {
            int i = it - event.begin();
            // std::cout<< i;
            pos = event.at(i).find(":");
            if (pos == std::string::npos)
                continue;

            std::string lineheader = event.at(i).substr(0, pos);
            bool found = false;
            if (descriptionByUser.at(userName).substr(start, end).find(lineheader) != std::string::npos)
                found = true;
            if (found)
            {
                std::cout << "updating stat";
                std::string temp = event[i];
                size_t pos2 = descriptionByUser[userName].find(lineheader);
                size_t end_pos = descriptionByUser.at(userName).find('\n', pos2);
                descriptionByUser.at(userName).replace(pos2, end_pos - pos2, temp);

            }//if its a new stat
            else
            {
                std::cout << "new stat";
                updates.push_back(event[i]);

            }
        }
        ogstring = descriptionByUser[userName] + "\0";//after the replacemnt
        delimiter = "\n";
        position = descriptionByUser.at(userName).find("team b updates:");
        token = " ";
        start_position = descriptionByUser.at(userName).find(delimiter, start);
        while ((position = descriptionByUser.at(userName).find(delimiter, start_position+1)) != std::string::npos) {
            token = descriptionByUser.at(userName).substr(start_position + 1, position - (start_position + delimiter.size()));
            if (token == "")
                break;
            updates.push_back(token);
            descriptionByUser.at(userName).erase(start_position, token.length() + delimiter.length());
            start_position = position - (token.length() + delimiter.length());
        }
        std::sort(updates.begin(), updates.end());
        ans = "";
        size = updates.size();
        for (int i = 0; i < size; i++) {
            ans = ans + updates[i] + "\n";
        }
        descriptionByUser.at(userName) = split(ogstring, "team b updates:", "endofstring", ans);
        updates.clear();// team b des parsed
        std::string description = "";
        for (auto it = descpos; it < event.end(); it++) {
            int i = std::distance(event.begin(), it);
            description = description + event[i] + "\n";
        }
        std::string toAddEvent = time + " - " + eventName + "\n"
            + "\n"
            + description;
        add_event(userName, toAddEvent);
    }








    // Removes an event from the list of events for the specified user.
    // Returns true if the event was found and removed, false otherwise.
    bool  remove_event(std::string userName, const std::string& event) {
        std::lock_guard<std::mutex> lock(m);
        auto it = events_by_user.find(userName);
        if (it == events_by_user.end()) {
            return false;
        }
        auto& events = it->second;
        auto event_it = std::find(events.begin(), events.end(), event);
        if (event_it == events.end()) {
            return false;
        }
        events.erase(event_it);
        return true;
    }

    std::vector<std::string> get_users() {
        std::lock_guard<std::mutex> lock(m);
        return std::vector<std::string>(users.begin(), users.end());
    }

    std::string split(std::string og, std::string cutter, std::string cutter2, std::string ans) {
        std::size_t pos = og.find(cutter)+cutter.size();
        std::size_t pos2 = og.length();
        if (cutter2 != "endofstring")
            pos2 = og.find(cutter2);
        std::string string1 = og.substr(0, pos);
        std::string string2 = og.substr(pos2);
        std::string final_string = string1 + "\n" + ans + string2;
        return final_string;
    }

};




