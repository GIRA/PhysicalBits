
#pragma once

#include "Monitor.h"
#include "GPIO.h"
#include "StackArray.h"
#include "Program.h"
#include "Errors.h"

class VM
{

public:
	Error executeProgram(Program*, GPIO*, Monitor*);

private:

	int16 framePointer = 0;
	int16 pc = 0;
	StackArray stack;
	Program* currentProgram = 0;
	Coroutine* currentCoroutine = 0;
	Script* currentScript = 0;

	Instruction nextInstruction(void);
	void executeInstruction(Instruction, GPIO*, Monitor*, bool&);
	void executeCoroutine(Coroutine*, GPIO*, Monitor*);
	void yieldTime(int32, bool&);
	void unwindStackAndReturn(void);
};

