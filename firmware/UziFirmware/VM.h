
#pragma once

class Monitor;

#include "GPIO.h"
#include "Memory.h"
#include "Program.h"
#include "Errors.h"



#ifdef __SIMULATOR__
#include "NewPing_mock.h"
#include "LiquidCrystal_I2C_mock.h"
#else
#include "NewPing.h"
#include "LiquidCrystal_I2C.h"
#endif // __SIMULATOR__


class VM
{

public:
	Error executeProgram(Program*, GPIO*, Monitor*);

	// TODO(Richo): Compress into one uint16
	bool halted = false;
	Script* haltedScript = 0;

	void reset();
private:

	int32 lastTickStart;

	float returnValue = 0;
	int16 framePointer = 0;
	int16 pc = 0;
	
	Program* currentProgram = 0;
	Coroutine* currentCoroutine = 0;
	Script* currentScript = 0;

	/*
	TODO(Richo): Look over all error references to make sure I'm not supressing
	errors. This is definitely an existing problem but I'm leaving this for later.
	*/
	Error error = NO_ERROR;

	void executeInstruction(Instruction, GPIO*, Monitor*, bool&);
	void handleBackwardJump(const int16& argument, bool& yieldFlag);
	void executeCoroutine(Coroutine*, GPIO*, Monitor*);
	void saveCurrentCoroutine();
	void yieldTime(int32, bool&);
	void unwindStackAndReturn(void);
};

// TODO(Richo): Redesign Monitor and VM to avoid coupling
#include "Monitor.h"
