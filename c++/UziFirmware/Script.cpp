#include "Script.h"

Error readScript(Reader * rs, Script* script, uint8 start, uint8 scriptIndex, float* globals)
{
	script->instructionStart = start;
	script->index = scriptIndex;

	script->stepping = false;
	script->interval = 0;
	script->argCount = script->localCount = 0;
	script->locals = 0; 
	script->instructionCount = 0;
	script->instructions = 0;
	script->coroutine = 0;


	bool timeout;
	uint8 h = rs->next(timeout);
	if (timeout) return READER_TIMEOUT;

	script->stepping = (h >> 7) & 1;
	
	if ((h >> 6) & 1) // Has delay
	{
		uint8 index = rs->next(timeout);
		if (timeout) return READER_TIMEOUT;
		script->interval = globals[index];
	}

	if ((h >> 5) & 1) // Has arguments
	{
		script->argCount = rs->next(timeout);
		if (timeout) return READER_TIMEOUT;
	}

	if ((h >> 4) & 1) // Has locals
	{
		script->localCount = rs->next(timeout);
		if (timeout) return READER_TIMEOUT;
		
		if (script->localCount > 0)
		{
			script->locals = uzi_createArray(float, script->localCount);
			if (script->locals == 0) return OUT_OF_MEMORY;

			for (int i = 0; i < script->localCount; i++)
			{
				uint8 index = rs->next(timeout);
				if (timeout) return READER_TIMEOUT;
				script->locals[i] = globals[index];
			}
		}
	}

	script->instructionCount = rs->next(timeout);
	if (timeout) return READER_TIMEOUT;

	if (script->instructionCount > 0)
	{
		script->instructions = uzi_createArray(Instruction, script->instructionCount);
		if (script->instructions == 0) return OUT_OF_MEMORY;

		for (int i = 0; i < script->instructionCount; i++)
		{
			readInstruction(rs, &script->instructions[i], timeout);
			if (timeout) return READER_TIMEOUT;
		}
	}

	return NO_ERROR;
}

int8 Script::getInstructionStart(void)
{
	return instructionStart;
}

int8 Script::getInstructionStop(void)
{
	return instructionStart + instructionCount - 1;
}

uint8 Script::getInstructionCount(void)
{
	return instructionCount;
}

Instruction Script::getInstructionAt(int16 index)
{
	return instructions[index - instructionStart];
}

bool Script::isStepping(void)
{
	return stepping;
}

void Script::setStepping(bool val)
{
	stepping = val;
}

float Script::getInterval(void)
{
	return interval;
}

uint8 Script::getArgCount(void)
{
	return argCount;
}

uint8 Script::getLocalCount(void)
{
	return localCount;
}

float Script::getLocal(uint8 index)
{
	return locals[index];
}

uint8 Script::getIndex(void)
{
	return index;
}

Coroutine* Script::getCoroutine(void) 
{
	if (coroutine == 0) 
	{
		coroutine = uzi_create(Coroutine);
		if (coroutine == 0) return 0; // TODO(Richo): Notify user of OUT_OF_MEMORY!

		coroutine->script = this;
		coroutine->activeScript = this;
		coroutine->framePointer = -1;
		coroutine->pc = instructionStart;
		coroutine->nextRun = 0;
		coroutine->breakCount = -1;
		coroutine->error = NO_ERROR;
		coroutine->stackSize = 0;
		coroutine->stackAllocated = estimateStackSize();
		if (coroutine->stackAllocated == 0)
		{
			coroutine->stackElements = 0;
		}
		else 
		{
			coroutine->stackElements = uzi_createArray(float, coroutine->stackAllocated);
			if (coroutine->stackElements == 0)
			{
				coroutine->setError(OUT_OF_MEMORY);
				coroutine->stackAllocated = coroutine->stackSize = 0;
				return 0;
			}
		}
	}
	return coroutine;
}

uint16 Script::estimateStackSize() 
{
	// INFO(Richo): We always start with 2 elements (return value, fp + pc)
	int16 total = 2;
	int16 max = total;
	for (int i = 0; i < instructionCount; i++)
	{
		uint8 opcode = instructions[i].opcode;
		int8 argument = instructions[i].argument;

		// Handle jumps (forward: always true, backward: always false)
		if (opcode == JMP
			|| opcode == JZ
			|| opcode == JNZ
			|| opcode == JNE
			|| opcode == JLT
			|| opcode == JLTE
			|| opcode == JGT
			|| opcode == JGTE)
		{
			i += (argument > 0 ? argument : 0);
		}

		int8 stackImpact = (opcode >> 6) - 2;
		if (opcode == SCRIPT_CALL) 
		{
			stackImpact = 2; // TODO(Richo): Locals are ignored for now
		}
		total += stackImpact;
				
		bool contextSwitch = false; // Is a context switch possible at this point?
		
		/*
		INFO(Richo): Although SCRIPT_CALL doesn't force a context switch, the called script
		could contain an instruction that does.
		*/
		contextSwitch |= (opcode == SCRIPT_CALL);

		/*
		INFO(Richo): SCRIPT_STOP and SCRIPT_START could also force a context switch but they
		reset the stack anyway so the estimated value for those opcodes should be 0.
		*/
		contextSwitch |= (opcode == SCRIPT_PAUSE && argument == index);

		/*
		INFO(Richo): Only backward jumps force a context switch.
		*/
		if (opcode == JMP
			|| opcode == JZ
			|| opcode == JNZ
			|| opcode == JNE
			|| opcode == JLT
			|| opcode == JLTE
			|| opcode == JGT
			|| opcode == JGTE) 
		{
			contextSwitch |= (argument < 0);
		}

		// INFO(Richo): Yield and delays 
		contextSwitch |= (opcode == PRIM_YIELD
			|| opcode == PRIM_DELAY_MILLIS
			|| opcode == PRIM_DELAY_SECONDS
			|| opcode == PRIM_DELAY_MINUTES);


		if (contextSwitch && total > max) { max = total; }
		// TODO(Richo): As tera suggested, total should never go below zero, we could check that and raise a warning or something...
	}
	// TODO(Richo): When all instructions are calculated correctly, total should be 0
	return max < 0 ? 0 : max;
}

bool Script::hasCoroutine(void) 
{
	return coroutine != 0;
}