#include "Instruction.h"

Instruction::Instruction(Reader* rs, bool& timeout)
{
	unsigned char bytecode = rs->next(timeout);
	if (timeout) return;

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
}

unsigned char Instruction::getOpcode(void)
{
	return opcode;
}

unsigned short Instruction::getArgument(void)
{
	return argument;
}