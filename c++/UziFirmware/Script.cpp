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
	if (!timeout) { parseInstructions(rs, timeout); }

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

void Script::parseInstructions(Reader* rs, bool& timeout)
{
	instructions = new Instruction[instructionCount];
	for (int16 i = 0; i < instructionCount; i++)
	{
		uint8 bytecode = rs->next(timeout);
		if (timeout) return;

		uint8 opcode;
		int16 argument;
		if (bytecode < 0x80)
		{
			/*
			If the high-order bit is zero (< 0x8) then the opcode is stored in the 3 msbits
			and the argument is stored in the 5 lsbits.
			*/
			opcode = bytecode >> 5;
			argument = bytecode & 0x1F;
		}
		else
		{
			/*
			If the high-order bit is one (>= 0x8) then the opcode is stored in the 4 msbits
			and the argument is stored in the 4 lsbits.
			*/
			opcode = bytecode >> 4;
			argument = bytecode & 0xF;
			if (0xF == opcode)
			{
				/*
				Special case: If the 4 msbits happen to be 0xF then the argument is stored
				on the next byte.
				TODO(Richo): Implement extend instruction
				*/
				opcode = bytecode;
				argument = rs->next(timeout);
				if (timeout) return;

				// Argument is encoded in two's complement
				if (argument >= 128)
				{
					argument = (0xFF & ((argument ^ 0xFF) + 1)) * -1;
				}
			}
		}
		instructions[i].opcode = opcode;
		instructions[i].argument = argument;
	}
}
