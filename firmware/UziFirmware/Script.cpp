#include "Script.h"

Error readScript(Reader * rs, Script* script, int16 start, uint8 scriptIndex, float* globals)
{
	script->instructionStart = start;
	script->index = scriptIndex;

	script->running = false;
	script->once = false;
	script->interval = 0;
	script->argCount = script->localCount = 0;
	script->locals = 0; 
	script->instructionCount = 0;
	script->coroutine = 0;

	bool timeout;
	uint8 h = rs->next(timeout);
	if (timeout) return READER_TIMEOUT;

	script->running = (h >> 7) & 1;
	script->once = ((h >> 6) & 1);
	
	if ((h >> 5) & 1) // Has delay
	{
		uint8 index = rs->next(timeout);
		if (timeout) return READER_TIMEOUT;
		script->interval = index;
	}

	if ((h >> 4) & 1) // Has arguments
	{
		script->argCount = rs->next(timeout);
		if (timeout) return READER_TIMEOUT;
	}

	if ((h >> 3) & 1) // Has locals
	{
		script->localCount = rs->next(timeout);
		if (timeout) return READER_TIMEOUT;
		
		if (script->localCount > 0)
		{
			script->locals = uzi_createArray(uint8, script->localCount);
			if (script->locals == 0) return OUT_OF_MEMORY;

			for (int i = 0; i < script->localCount; i++)
			{
				uint8 index = rs->next(timeout);
				if (timeout) return READER_TIMEOUT;
				script->locals[i] = index;
			}
		}
	}


	uint8 ic_h = rs->next(timeout);
	if (timeout) return READER_TIMEOUT;
	if (ic_h > 0x7F)
	{
		uint8 ic_l = rs->next(timeout);
		if (timeout) return READER_TIMEOUT;

		script->instructionCount = ((ic_h & 0x7F) << 8) | ic_l;
	}
	else
	{
		script->instructionCount = ic_h;
	}


	return NO_ERROR;
}

int16 Script::getInstructionStart(void)
{
	return instructionStart;
}

int16 Script::getInstructionStop(void)
{
	return instructionStart + instructionCount - 1;
}

uint16 Script::getInstructionCount(void)
{
	return instructionCount;
}

bool Script::isRunning(void)
{
	return running;
}

void Script::setRunning(bool val)
{
	running = val;
}

uint8 Script::getArgCount(void)
{
	return argCount;
}

uint8 Script::getLocalCount(void)
{
	return localCount;
}

uint8 Script::getLocal(uint8 index)
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
		if (coroutine == 0) return 0; // TODO(Richo): Notify user of OUT_OF_MEMORY!

		coroutine->scriptIndex = index;
		coroutine->activeScriptIndex = index;
		coroutine->framePointer = -1;
		coroutine->pc = instructionStart;
		coroutine->nextRun = 0;
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
