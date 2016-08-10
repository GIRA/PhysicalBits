
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

	GPIO * io;

	int pc;
	StackArray * stack;
	Script * currentScript;

	unsigned char nextBytecode(void);
	void executeBytecode(unsigned char);
	void executePrimitive(unsigned char);
	void executeScript(Script*, GPIO*);

};

