
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
	Script * currentScript;

	unsigned char nextBytecode(void);
	void executeBytecode(unsigned char, GPIO*);
	void executePrimitive(unsigned char, GPIO*);
	void executeScript(Script*, GPIO*);

};

