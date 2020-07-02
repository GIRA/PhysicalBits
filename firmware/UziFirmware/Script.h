#pragma once

#include "Reader.h"
#include "Instruction.h"
#include "Coroutine.h"
#include "Memory.h"

// TODO(Richo): Optimize memory!
struct Script
{
	bool stepping : 1;
	uint8 index : 7;
	uint8 interval = 0;

	uint8 argCount = 0;

	uint8 localCount = 0;
	uint8* locals = 0;

	int16 instructionStart = 0;
	uint16 instructionCount = 0;
	Instruction* instructions = 0;

	Coroutine* coroutine = 0;

	uint8 getIndex(void);
	int16 getInstructionStart(void);
	int16 getInstructionStop(void);
	uint16 getInstructionCount(void);
	Instruction getInstructionAt(int16);
	bool isStepping(void);
	void setStepping(bool);
	
	uint8 getArgCount(void);

	uint8 getLocalCount(void);
	uint8 getLocal(uint8);

	Coroutine* getCoroutine(void);
	bool hasCoroutine(void);
	void setBreakpointAt(int16, bool);
};

Error readScript(Reader * rs, Script* script, int16 start, uint8 scriptIndex, float* globals);