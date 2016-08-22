#pragma once

#include "Reader.h"
#include "Instruction.h"

class Script
{

public:
	Script(void);
	Script(Reader*, bool&);
	Script(const Script&);
	Script& operator=(const Script&);
	~Script(void);

	unsigned char getInstructionCount(void);
	Instruction getInstructionAt(int);
	bool shouldStepNow(long);
	void rememberLastStepTime(long);
	bool isStepping(void);
	void setStepping(bool);
	long getStepTime(void);

private:

	bool stepping;
	long stepTime;
	long lastStepTime;

	Instruction* instructions;
	unsigned char instructionCount;

	void parseInstructions(Reader*, bool&);
};

