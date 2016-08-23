#pragma once

#include "Reader.h"

typedef struct Instruction_s
{
	unsigned char opcode;
	unsigned short argument;
};

class Script
{

public:
	Script(void);
	Script(Reader*, bool&);
	Script(const Script&);
	Script& operator=(const Script&);
	~Script(void);

	unsigned char getInstructionCount(void);
	Instruction_s getInstructionAt(int);
	bool shouldStepNow(long);
	void rememberLastStepTime(long);
	bool isStepping(void);
	void setStepping(bool);
	long getStepTime(void);

private:

	bool stepping;
	long stepTime;
	long lastStepTime;

	Instruction_s* instructions;
	unsigned char instructionCount;

	void parseInstructions(Reader*, bool&);
};

