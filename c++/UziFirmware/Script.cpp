#include "Script.h"

void readScript(Reader * rs, Script* script, uint8 start, uint8 scriptIndex, float* globals, bool& timeout)
{
	script->instructionStart = start;
	script->index = scriptIndex;

	script->stepping = false;
	script->interval = 0;
	script->argCount = script->localCount = 0;
	script->locals = 0; 
	script->instructionCount = 0;
	script->instructions = 0;
	script->coroutine = 0;

	uint8 h = rs->next(timeout);
	if (timeout) return;

	script->stepping = (h >> 7) & 1;
	
	if ((h >> 6) & 1) // Has delay
	{
		uint8 index = rs->next(timeout);
		if (timeout) return;
		script->interval = globals[index];
	}

	if ((h >> 5) & 1) // Has arguments
	{
		script->argCount = rs->next(timeout);
		if (timeout) return;
	}

	if ((h >> 4) & 1) // Has locals
	{
		script->localCount = rs->next(timeout);
		if (timeout) return;
		script->locals = uzi_createArray(float, script->localCount);
		for (int i = 0; i < script->localCount; i++)
		{
			uint8 index = rs->next(timeout);
			if (timeout) return;
			script->locals[i] = globals[index];
		}
	}

	script->instructionCount = rs->next(timeout);
	if (timeout) return;

	script->instructions = uzi_createArray(Instruction, script->instructionCount);
	for (int i = 0; i < script->instructionCount; i++) 
	{
		readInstruction(rs, &script->instructions[i], timeout);
		if (timeout) return;
	}
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
		coroutine = uzi_create(Coroutine);

		coroutine->script = this;
		coroutine->activeScript = this;
		coroutine->framePointer = -1;
		coroutine->pc = instructionStart;
		coroutine->nextRun = 0;
		coroutine->breakCount = -1;
		coroutine->error = NO_ERROR;
		coroutine->stackSize = 0;
		coroutine->stackAllocated = 0;
		coroutine->stackElements = 0;
	}
	return coroutine;
}

bool Script::hasCoroutine(void) 
{
	return coroutine != 0;
}