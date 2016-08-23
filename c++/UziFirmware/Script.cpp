#include "Script.h"

Script::Script(Reader * rs, bool& timeout)
{
	long n = rs->nextLong(4, timeout);
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

unsigned char Script::getInstructionCount(void)
{
	return instructionCount;
}

Instruction_s Script::getInstructionAt(int index)
{
	return instructions[index];
}

void Script::rememberLastStepTime(long now)
{
	lastStepTime = now;
}

bool Script::shouldStepNow(long now)
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

long Script::getStepTime(void)
{
	return stepTime;
}

void Script::parseInstructions(Reader* rs, bool& timeout)
{
	instructions = new Instruction_s[instructionCount];
	for (int i = 0; i < instructionCount; i++)
	{
		unsigned char bytecode = rs->next(timeout);
		if (timeout) return;

		unsigned char opcode;
		unsigned short argument;
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
				if (argument >= 2)
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
