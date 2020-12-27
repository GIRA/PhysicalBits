
#pragma once

class Monitor;

#include "GPIO.h"
#include "StackArray.h"
#include "Program.h"
#include "Errors.h"

#ifdef __SIMULATOR__
#include "NewPing_mock.h"
#else
#include "NewPing.h"
#endif // __SIMULATOR__


class VM
{

public:
	Error executeProgram(Program*, GPIO*, Monitor*);

	// TODO(Richo): Compress into one uint16
	bool halted = false;
	Script* haltedScript = 0;

	void reset();
	uint32 stackAvailable(void);
private:

	int32 lastTickStart;

	float returnValue = 0;
	int16 framePointer = 0;
	int16 pc = 0;
	StackArray stack;
	Program* currentProgram = 0;
	Coroutine* currentCoroutine = 0;
	Script* currentScript = 0;

	void executeInstruction(Instruction, GPIO*, Monitor*, bool&);
	void executeCoroutine(Coroutine*, GPIO*, Monitor*);
	void saveCurrentCoroutine();
	void yieldTime(int32, bool&);
	void unwindStackAndReturn(void);
};

// TODO(Richo): Redesign Monitor and VM to avoid coupling
#include "Monitor.h"
