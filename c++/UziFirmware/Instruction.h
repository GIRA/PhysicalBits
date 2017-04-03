#pragma once

#include "Reader.h"

enum Opcode
{
	TURN_ON,
	TURN_OFF,
	WRITE_PIN,
	READ_PIN,
	READ_GLOBAL,
	WRITE_GLOBAL,
	PRIM_CALL,
	SCRIPT_CALL,
	SCRIPT_START,
	SCRIPT_STOP,
	JMP,
	JZ,
	JNZ,
	JNE,
	JLT,
	JLTE,
	JGT,
	JGTE,
	READ_LOCAL,
	WRITE_LOCAL
};

struct Instruction
{
	Opcode opcode;
	int16 argument;
};

Instruction* readInstructions(Reader* rs, uint8 count, bool& timeout);
