#include "Script.h"

Script::Script(uint8 start, Reader * rs, bool& timeout)
{
	instructionStart = start;
	int32 n = rs->nextLong(4, timeout);
	if (!timeout)
	{
		stepping = (n >> 31) & 1;
		stepTime = n & 0x7FFFFFFF;
	}
	if (!timeout) { instructionCount = rs->next(timeout); }
	if (!timeout) { instructions = readInstructions(rs, instructionCount, timeout); }

	nextScript = 0;
}

Script::Script()
{
	// Initializes current script as NOP
	stepping = false;
	stepTime = instructionCount = 0;
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
