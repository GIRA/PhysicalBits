#pragma once

#include "Reader.h"
#include "Instruction.h"
#include "Coroutine.h"
#include "Memory.h"

enum ScriptType
{
	TASK = 0,
	FUNC = 1,
	PROC = 2,
	TIMER = 3
};

// TODO(Richo): Optimize memory!
struct Script
{
	uint8 index;
	uint8 interval;

	bool running : 1;
	uint8 type : 2;
	uint8 argCount : 5;

	uint8 localCount = 0;
	uint8* locals = 0;

	int16 instructionStart = 0;
	uint16 instructionCount = 0;

	Coroutine* coroutine = 0;

	uint8 getIndex(void);
	int16 getInstructionStart(void);
	int16 getInstructionStop(void);
	uint16 getInstructionCount(void);
	bool isRunning(void);
	void setRunning(bool);
	
	uint8 getArgCount(void);

	uint8 getLocalCount(void);
	uint8 getLocal(uint8);

	Coroutine* getCoroutine(void);
	bool hasCoroutine(void);
};

Error readScript(Reader * rs, Script* script, int16 start, uint8 scriptIndex, float* globals);