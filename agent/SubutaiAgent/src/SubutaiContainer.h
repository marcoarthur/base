/**
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    @copyright 2014 Safehaus.org
 */
/**
 *  @brief     SubutaiEnvironment.h
 *  @class     SubutaiEnvironment.h
 *  @details   SubutaiEnvironment Class is designed for getting and setting environment variables and special informations.
 *  		   This class's instance can get get useful Agent's specific Environment informations
 *  		   such us IPs, UUID, hostname, macID, parentHostname, etc..
 *  @author    Mikhail Savochkin
 *  @author    Ozlem Ceren Sahin
 *  @version   1.1.0
 *  @date      Oct 31, 2014
 */
#ifndef SUBUTAICONTAINER_H_
#define SUBUTAICONTAINER_H_
#include <syslog.h>
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <cstdlib>
#include <sstream>
#include <list>
#include <lxc/lxccontainer.h>
#include "pugixml.hpp"
#include <stdio.h>
#include <unistd.h>
#include <sys/stat.h>
#include <iostream>
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_generators.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/thread/thread.hpp>
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/ini_parser.hpp>
#include "SubutaiLogger.h"
#include "SubutaiConnection.h"
#include "SubutaiCommand.h"
#include "SubutaiHelper.h"
using namespace std;
using std::stringstream;
using std::string;

typedef map<int, string>::iterator user_it;

// Execution Result contains exit code of a functions, stdout and/or stderr
struct ExecutionResult {
    string out;
    string err;
    int exit_code;
};



enum containerStatus { RUNNING, STOPPED, FROZEN };

class SubutaiContainer
{
    public:
        SubutaiContainer(SubutaiLogger*, lxc_container* cont);
        virtual ~SubutaiContainer(void);
        bool getContainerId();
        void tryLongCommand();
        void UpdateUsersList();
        void getContainerAllFields();
        bool getContainerInterfaces();
        void setContainerHostname(string);
        void setContainerStatus(containerStatus);
        void write();
        void clear();
        bool checkCWD(string cwd);
        bool checkUser(string username);
        int getRunAsUserId(string username);
        void PutToFile(string filename, string text);
        vector<Interface> getContainerInterfaceValues();
        lxc_container* getLxcContainerValue();
        string getContainerStatus();
        string getContainerIdValue();
        string getContainerHostnameValue();
        string RunPsCommand();
        string findFullProgramPath(string );
        string RunProgram(string , vector<string>);
        ExecutionResult RunCommand(SubutaiCommand* command);
        ExecutionResult RunDaemon(SubutaiCommand* command);
        ExecutionResult RunProgram(string , vector<string>, bool return_result, lxc_attach_options_t opts = LXC_ATTACH_OPTIONS_DEFAULT, bool captureOutput = true);

    protected:
        vector<string> ExplodeCommandArguments(SubutaiCommand* command);
    private:
        containerStatus 	status;
        lxc_container* 		container;
        string 				id;
        string 				hostname;
        map<int, string> 	_users;        // List of users available in system
        vector<Interface> 	interfaces;
        SubutaiLogger*		containerLogger;
        SubutaiHelper 		_helper;
};
#endif /* SUBUTAICONTAINER_H_ */



