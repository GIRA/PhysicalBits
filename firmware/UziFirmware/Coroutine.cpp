#include "Coroutine.h"

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

Error Coroutine::saveStack()
{
	stackSize = stack_size();
	if (stackSize > stackAllocated) // We need to grow!
	{
#ifdef __SIMULATOR__
		Stats.coroutineResizeCounter++;
#endif // __SIMULATOR__

		stackAllocated = stackSize;
		stackElements = uzi_createArray(float, stackAllocated);
		if (stackElements == 0) 
		{
			stackAllocated = stackSize = 0;			
			return OUT_OF_MEMORY;
		}
	}

	stack_saveTo(stackElements); 
	return NO_ERROR;
}

Error Coroutine::restoreStack()
{
	Error error;
	stack_restoreFrom(stackElements, stackSize, error);
	return error;
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
