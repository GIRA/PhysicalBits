
#pragma once

class Monitor;

#include "GPIO.h"
#include "StackArray.h"
#include "Program.h"
#include "Errors.h"

class VM
{

public:
	Error executeProgram(Program*, GPIO*, Monitor*);

	// TODO(Richo): Compress into one uint16
	bool halted = false;
	Script* haltedScript = 0;
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

// TODO(Richo): Redesign Monitor and VM to avoid coupling
#include "Monitor.h"
