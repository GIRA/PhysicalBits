#pragma once

#include "Reader.h"
#include "Script.h"
#include "Coroutine.h"
#include "GPIO.h"

class Program
{

public:
	Program(Reader*, bool&);
	Program(void);
	~Program(void);

	uint8 getScriptCount(void);
	Script* getScript(int16);
	Script* getScriptForPC(int16);
	
	float getGlobal(int16);
	void setGlobal(int16, float);
	bool getReport(uint8);
	void setReport(uint8, bool);
	uint8 getGlobalCount(void);

private:

	uint8 scriptCount;
	Script* scripts;

	uint8 globalCount;
	float* globals;
	uint8* globalsReport;

	void parseGlobals(Reader*, bool&);
	void parseScripts(Reader*, bool&);
};

