#pragma once

#include "Reader.h"
#include "Script.h"
#include "GPIO.h"

class Program
{

public:
	Program(Reader*, bool&);
	Program(void);
	~Program(void);

	unsigned char getScriptCount(void);
	Script * getScript(void);
	
	long getGlobal(int);
	void setGlobal(int, long);

private:

	unsigned char scriptCount;
	Script * script;

	long * globals;

	void parseGlobals(Reader*, bool&);
	void parseScripts(Reader*, bool&);
};

