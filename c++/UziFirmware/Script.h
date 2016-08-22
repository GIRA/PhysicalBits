#pragma once

#include "Reader.h"
#include "Instruction.h"

class Script
{

public:
	Script(Reader*, bool&);
	Script(void);
	~Script(void);

	unsigned char getInstructionCount(void);
	Instruction getInstructionAt(int);
	bool shouldStepNow(long);
	void rememberLastStepTime(long);
	bool isStepping(void);
	void setStepping(bool);
	long getStepTime(void);
	void setNext(Script*);
	Script* getNext(void);

private:

	bool stepping;
	long stepTime;
	long lastStepTime;

	Instruction* instructions;
	unsigned char instructionCount;

	Script * nextScript;

	void parseInstructions(Reader*, bool&);
};

