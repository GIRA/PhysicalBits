#include "Coroutine.h"
#include "Script.h"

Script* Coroutine::getScript(void)
{
	return script;
}

Script* Coroutine::getActiveScript(void)
{
	return activeScript;
}

void Coroutine::setActiveScript(Script* value)
{
	activeScript = value;
}

int16 Coroutine::getFramePointer(void)
{
	return framePointer;
}

void Coroutine::setFramePointer(int16 value)
{
	framePointer = value;
}

int16 Coroutine::getPC(void)
{
	return pc;
}

void Coroutine::setPC(int16 value)
{
	pc = value;
}

void Coroutine::saveStack(StackArray* stack)
{
	stackSize = stack->getPointer();
	if (stackSize > stackAllocated) // We need to grow!
	{
#ifdef __SIMULATOR__
		Stats.coroutineResizeCounter++;
#endif // __SIMULATOR__

		stackAllocated = stackSize;
		stackElements = uzi_createArray(float, stackAllocated);
		if (stackElements == 0) 
		{
			setError(OUT_OF_MEMORY);
			stackAllocated = stackSize = 0;			
			return;
		}
	}

	stack->copyTo(stackElements); 
}

void Coroutine::restoreStack(StackArray* stack)
{
	stack->copyFrom(stackElements, stackSize); 
}

int32 Coroutine::getNextRun(void)
{
	return nextRun;
}

void Coroutine::setNextRun(int32 value)
{
	nextRun = value;
}

int32 Coroutine::getLastStart() 
{
	return lastStart;
}

void Coroutine::setLastStart(int32 value)
{
	lastStart = value;
}

uint16 Coroutine::getStackSize(void)
{
	return stackSize;
}

float Coroutine::getStackElementAt(uint16 index)
{
	if (index >= stackSize) return 0;
	return stackElements[index];
}

bool Coroutine::hasError()
{
	return error != NO_ERROR;
}

Error Coroutine::getError(void)
{
	return error;
}

void Coroutine::setError(Error err)
{
	error = err;
	script->setRunning(false);
}

void Coroutine::reset(void)
{
	error = NO_ERROR;
	activeScript = script;
	framePointer = -1;
	pc = script->getInstructionStart();
	stackSize = 0;

}