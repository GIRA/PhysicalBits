#pragma once

#include "Reader.h"
#include "Script.h"
#include "PE.h"

class Program
{

public:
	Program(Reader*);
	Program(void);
	~Program(void);

	unsigned char getScriptCount(void);
	Script * getScript(void);
	void configurePins(PE*);

private:

	unsigned char _scriptCount;
	Script * _script;

	void parseScripts(Reader*);
};

