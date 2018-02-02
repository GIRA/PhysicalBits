#pragma once

#include "Reader.h"
#include "Script.h"
#include "Coroutine.h"
#include "GPIO.h"

struct Program
{
	uint8 scriptCount = 0;
	Script* scripts = 0;

	uint8 globalCount = 0;
	float* globals = 0;
	uint8* globalsReport = 0;

	uint8 getScriptCount(void);
	Script* getScript(int16);
	Script* getScriptForPC(int16);
	
	float getGlobal(int16);
	void setGlobal(int16, float);
	bool getReport(uint8);
	void setReport(uint8, bool);
	uint8 getGlobalCount(void);
};

void readProgram(Reader* rs, Program* program, bool& timeout);