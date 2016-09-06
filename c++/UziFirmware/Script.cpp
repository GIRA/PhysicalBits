#include "Script.h"

Script::Script(Reader * rs, bool& timeout)
{
	int32 n = rs->nextLong(4, timeout);
	if (!timeout)
	{
		stepping = (n >> 31) & 1;
		stepTime = n & 0x7FFFFFFF;
		lastStepTime = 0;
	}
	if (!timeout) { instructionCount = rs->next(timeout); }
	if (!timeout) { parseInstructions(rs, timeout); }

	nextScript = 0;
}

Script::Script()
{
	// Initializes current script as NOP
	stepping = false;
	stepTime = lastStepTime = instructionCount = 0;
	instructions = 0;
	nextScript = 0;
}

Script::~Script(void)
{
	delete[] instructions;
	delete nextScript;
}

uint8 Script::getInstructionCount(void)
{
	return instructionCount;
}

Instruction Script::getInstructionAt(int16 index)
{
	return instructions[index];
}

void Script::rememberLastStepTime(int32 now)
{
	lastStepTime = now;
}

bool Script::shouldStepNow(int32 now)
{
	return (now - lastStepTime) >= stepTime;
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

void Script::parseInstructions(Reader* rs, bool& timeout)
{
	instructions = new Instruction[instructionCount];
	for (int16 i = 0; i < instructionCount; i++)
	{
		uint8 bytecode = rs->next(timeout);
		if (timeout) return;

		uint8 opcode;
		uint16 argument;
		if (bytecode < 0x80)
		{
			opcode = bytecode >> 5;
			argument = bytecode & 0x1F;
		}
		else
		{
			opcode = bytecode >> 4;
			argument = bytecode & 0xF;
			if (0xF == opcode)
			{
				opcode = bytecode;
				if (argument > 0)
				{
					argument = rs->next(timeout);
					if (timeout) return;
				}
			}
		}
		instructions[i].opcode = opcode;
		instructions[i].argument = argument;
	}
}
