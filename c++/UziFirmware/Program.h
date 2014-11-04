#pragma once

#include "ReadStream.h"
#include "Script.h"

class Program {

public:
	Program(ReadStream*);
	Program(void);
	~Program(void);
	
	unsigned char getScriptCount(void);
	Script * getScript(void);

private:
	
	unsigned char _pinModes[3];
	unsigned char _scriptCount;
	Script * _script;
	
	void parsePinModes(ReadStream*);
	void parseScripts(ReadStream*);
};

