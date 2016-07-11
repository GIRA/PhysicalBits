
#pragma once

#include "GPIO.h"
#include "StackArray.h"
#include "Program.h"

class VM
{

public:
	VM(void) 
	{
		_stack = new StackArray();
	}
	~VM(void)
	{
		delete _stack;
	}

	void executeProgram(Program*, GPIO*);

private:

	GPIO * _pe;

	int _ip;
	StackArray * _stack;
	Script * _currentScript;

	unsigned char nextBytecode(void);
	void executeBytecode(unsigned char);
	void executePrimitive(unsigned char);
	void executeScript(Script*, GPIO*);

};

