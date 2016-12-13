
#pragma once

#include "GPIO.h"
#include "StackArray.h"
#include "Program.h"

class VM
{

public:
	VM(void) 
	{
		pc = 0;
		stack = new StackArray();
	}
	~VM(void)
	{
		delete stack;
	}

	void executeProgram(Program*, GPIO*);

private:

	int16 pc;
	StackArray* stack;
	Program* currentProgram;
	Coroutine* currentCoroutine;
	Script* currentScript;

	Instruction nextInstruction(void);
	void executeInstruction(Instruction, GPIO*, bool& yieldFlag);
	void executePrimitive(uint16, GPIO*, bool& yieldFlag);
	void executeCoroutine(Coroutine*, GPIO*);

};

