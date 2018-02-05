
#pragma once

#include "GPIO.h"
#include "StackArray.h"
#include "Program.h"
#include "Errors.h"

class VM
{

public:
	void executeProgram(Program*, GPIO*);

private:

	int16 framePointer = 0;
	int16 pc = 0;
	StackArray stack;
	Program* currentProgram = 0;
	Coroutine* currentCoroutine = 0;
	Script* currentScript = 0;

	Instruction nextInstruction(void);
	void executeInstruction(Instruction, GPIO*, bool&);
	void executeCoroutine(Coroutine*, GPIO*);
	void yieldTime(int32, bool&);
	void unwindStackAndReturn(void);
};

