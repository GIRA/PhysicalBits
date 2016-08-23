
#pragma once

#include "GPIO.h"
#include "StackArray.h"
#include "Program.h"

class VM
{

public:
	VM(void) 
	{
		stack = new StackArray();
	}
	~VM(void)
	{
		delete stack;
	}

	void executeProgram(Program*, GPIO*);

private:

	int pc;
	StackArray * stack;
	Program * currentProgram;
	Script * currentScript;

	Instruction_s nextInstruction(void);
	void executeInstruction(Instruction_s, GPIO*);
	void executePrimitive(unsigned short, GPIO*);
	void executeScript(Script*, GPIO*);

};

