#pragma once

#include "Arduino.h"
#include "StackArray.h"
#include "Errors.h"

struct Script;

struct Coroutine
{
	Script* activeScript = 0;
	int16 framePointer = 0;
	int16 pc = 0;
	float* stackElements = 0;
	uint16 stackSize = 0;

	Script* script = 0;
	int32 nextRun = 0;

	int8 breakCount = 0;
	bool dumpState = false;

	Error error = NO_ERROR;


	Script* getScript(void);

	Script* getActiveScript(void);
	void setActiveScript(Script*);
	int16 getFramePointer(void);
	void setFramePointer(int16);
	int16 getPC(void);
	void setPC(int16);
	void saveStack(StackArray*);
	void restoreStack(StackArray*);
	int32 getNextRun(void);
	void setNextRun(int32);
	int8 getBreakCount(void);
	void setBreakCount(int8);
	bool getDumpState(void);
	void clearDumpState(void);
	uint16 getStackSize(void);
	float getStackElementAt(uint16);

	Error getError(void);
	void setError(Error);
	void reset(void);	
};