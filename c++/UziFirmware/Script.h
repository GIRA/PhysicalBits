#pragma once

#include "Reader.h"

struct Instruction
{
	uint8 opcode;
	uint16 argument;
};

class Script
{

public:
	Script(Reader*, bool&);
	Script(void);
	~Script(void);

	uint8 getInstructionCount(void);
	Instruction getInstructionAt(int16);
	bool shouldStepNow(int32);
	void rememberLastStepTime(int32);
	bool isStepping(void);
	void setStepping(bool);
	int32 getStepTime(void);
	void setNext(Script*);
	Script* getNext(void);

private:

	bool stepping;
	int32 stepTime;
	int32 lastStepTime;

	Instruction* instructions;
	uint8 instructionCount;

	Script * nextScript;

	void parseInstructions(Reader*, bool&);
};

