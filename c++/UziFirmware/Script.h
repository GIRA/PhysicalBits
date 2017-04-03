#pragma once

#include "Reader.h"
#include "Instruction.h"

class Script
{

public:
	Script(uint8, Reader*, bool&);
	Script(void);
	~Script(void);

	uint8 getInstructionStart(void);
	uint8 getInstructionStop(void);
	uint8 getInstructionCount(void);
	Instruction getInstructionAt(int16);
	bool isStepping(void);
	void setStepping(bool);
	int32 getStepTime(void);
	void setNext(Script*);
	Script* getNext(void);

private:

	bool stepping;
	int32 stepTime;

	uint8 instructionStart;
	uint8 instructionCount;
	Instruction* instructions;

	Script * nextScript;

	void parseInstructions(Reader*, bool&);
};

