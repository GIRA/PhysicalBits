#include "Script.h"

Script::Script(uint8 start, uint8 scriptIndex, float* globals, Reader * rs, bool& timeout)
{
	instructionStart = start;
	index = scriptIndex;

	stepping = false;
	interval = 0;
	argCount = localCount = 0;
	locals = 0; 
	instructionCount = 0;
	instructions = 0;
	coroutine = 0;

	uint8 h = rs->next(timeout);
	if (timeout) return;

	stepping = (h >> 7) & 1;
	
	if ((h >> 6) & 1) // Has delay
	{
		uint8 index = rs->next(timeout);
		if (timeout) return;
		interval = globals[index];
	}

	if ((h >> 5) & 1) // Has arguments
	{
		argCount = rs->next(timeout);
		if (timeout) return;
	}

	if ((h >> 4) & 1) // Has locals
	{
		localCount = rs->next(timeout);
		if (timeout) return;
		locals = new float[localCount];
		for (int i = 0; i < localCount; i++)
		{
			uint8 index = rs->next(timeout);
			if (timeout) return;
			locals[i] = globals[index];
		}
	}

	instructionCount = rs->next(timeout);
	if (timeout) return;

	instructions = readInstructions(rs, instructionCount, timeout);
	if (timeout) return;
}


Script::Script()
{
	// Initializes current script as NOP
	stepping = false;
	interval = 0;
	argCount = localCount = 0;
	locals = 0;
	instructionCount = 0;
	instructions = 0;
	coroutine = 0;
}

Script::~Script(void)
{
	delete[] locals;
	delete[] instructions;
	delete coroutine;
}

uint8 Script::getInstructionStart(void)
{
	return instructionStart;
}

uint8 Script::getInstructionStop(void)
{
	return instructionStart + instructionCount - 1;
}

uint8 Script::getInstructionCount(void)
{
	return instructionCount;
}

Instruction Script::getInstructionAt(int16 index)
{
	return instructions[index - instructionStart];
}

bool Script::isStepping(void)
{
	return stepping;
}

void Script::setStepping(bool val)
{
	stepping = val;
}

float Script::getInterval(void)
{
	return interval;
}

uint8 Script::getArgCount(void)
{
	return argCount;
}

uint8 Script::getLocalCount(void)
{
	return localCount;
}

float Script::getLocal(uint8 index)
{
	return locals[index];
}

uint8 Script::getIndex(void)
{
	return index;
}

Coroutine* Script::getCoroutine(void) 
{
	if (coroutine == 0) 
	{
		coroutine = new Coroutine(this);
	}
	return coroutine;
}

bool Script::hasCoroutine(void) 
{
	return coroutine != 0;
}