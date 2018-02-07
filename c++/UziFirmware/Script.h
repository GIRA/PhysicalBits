#pragma once

#include "Reader.h"
#include "Instruction.h"
#include "Coroutine.h"
#include "Memory.h"

struct Script
{
	uint8 index = 0;
	bool stepping = 0;
	float interval = 0;

	uint8 argCount = 0;

	uint8 localCount = 0;
	float* locals = 0;

	uint8 instructionStart = 0;
	uint8 instructionCount = 0;
	Instruction* instructions = 0;

	Coroutine* coroutine = 0;

	uint8 getIndex(void);
	uint8 getInstructionStart(void);
	uint8 getInstructionStop(void);
	uint8 getInstructionCount(void);
	Instruction getInstructionAt(int16);
	bool isStepping(void);
	void setStepping(bool);
	float getInterval(void);
	
	uint8 getArgCount(void);

	uint8 getLocalCount(void);
	float getLocal(uint8);

	Coroutine* getCoroutine(void);
	bool hasCoroutine(void);
};

Error readScript(Reader * rs, Script* script, uint8 start, uint8 scriptIndex, float* globals);