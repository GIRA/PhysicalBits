#pragma once

#include "Reader.h"
#include "Script.h"
#include "GPIO.h"

class Program
{

public:
	Program(Reader*);
	Program(void);
	~Program(void);

	unsigned char getScriptCount(void);
	Script * getScript(void);
	void configurePins(GPIO*);
	
	long getGlobal(int);
	void setGlobal(int, long);

private:

	unsigned char scriptCount;
	Script * script;

	long * globals;

	void parseGlobals(Reader*, bool&);
	void parseScripts(Reader*, bool&);
};

