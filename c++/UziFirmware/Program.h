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

private:

	unsigned char _scriptCount;
	Script * _script;

	void parseScripts(Reader*);
};

