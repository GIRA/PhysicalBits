#pragma once

#include "Reader.h"
#include "Instruction.h"

class Script
{

public:
	Script(uint8, uint8, float*, Reader*, bool&);
	Script(void);
	~Script(void);

	uint8 getIndex(void);
	uint8 getInstructionStart(void);
	uint8 getInstructionStop(void);
	uint8 getInstructionCount(void);
	Instruction getInstructionAt(int16);
	bool isStepping(void);
	void setStepping(bool);
	float getInterval(void);
	
	uint8 getArgCount(void);

	uint8 getLocalCount(void);
	float getLocal(uint8);

private:
	uint8 index;
	bool stepping;
	float interval;

	uint8 argCount;

	uint8 localCount;
	float* locals;

	uint8 instructionStart;
	uint8 instructionCount;
	Instruction* instructions;
};

