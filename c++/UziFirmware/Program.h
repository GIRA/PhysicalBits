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

	uint8 getScriptCount(void);
	Script * getScript(void);
	
	float getGlobal(int16);
	void setGlobal(int16, float);
	bool getReport(uint8);
	void setReport(uint8, bool);
	uint8 getGlobalCount(void);

private:

	uint8 scriptCount;
	Script * script;

	uint8 globalCount;
	float* globals;
	bool* globalsReport;

	void parseGlobals(Reader*, bool&);
	void parseScripts(Reader*, bool&);
};

