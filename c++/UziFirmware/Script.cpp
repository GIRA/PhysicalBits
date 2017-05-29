#include "Script.h"

Script::Script(uint8 start, uint8 scriptIndex, Reader * rs, bool& timeout)
{
	instructionStart = start;
	index = scriptIndex;
	int32 n = rs->nextLong(4, timeout);
	if (!timeout)
	{
		stepTime = n & 0x3FFFFFFF;
		stepping = (n >> 31) & 1;
		if ((n >> 30) & 1)
		{
			localCount = rs->next(timeout);
		}
		else
		{
			localCount = 0;
		}
	}
	if (!timeout) { instructionCount = rs->next(timeout); }
	if (!timeout) { instructions = readInstructions(rs, instructionCount, timeout); }

	nextScript = 0;
}

Script::Script()
{
	// Initializes current script as NOP
	stepping = false;
	stepTime = instructionCount = localCount = 0;
	instructions = 0;
	nextScript = 0;
}

Script::~Script(void)
{
	delete[] instructions;
	delete nextScript;
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

Script* Script::getNext(void)
{
	return nextScript;
}

void Script::setNext(Script* next)
{
	nextScript = next;
}

int32 Script::getStepTime(void)
{
	return stepTime;
}

uint8 Script::getLocalCount(void)
{
	return localCount;
}

uint8 Script::getIndex(void)
{
	return index;
}