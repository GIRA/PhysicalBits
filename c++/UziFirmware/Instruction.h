#pragma once

#include "Reader.h"

class Instruction
{
public:
	Instruction()
	{
		opcode = 0;
		argument = 0;
	}
	Instruction(Reader*, bool&);

	unsigned char getOpcode(void);
	unsigned short getArgument(void);

private:
	unsigned char opcode;
	unsigned short argument;

};