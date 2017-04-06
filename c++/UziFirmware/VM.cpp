#include "VM.h"

void VM::executeProgram(Program * program, GPIO * io)
{
	currentProgram = program;
	Coroutine * coroutine = 0;
	int16 count = program->getCoroutineCount();
	
	int32 now = millis();
	coroutine = program->getCoroutine();
	for (int16 i = 0; i < count; i++)
	{
		Script* script = coroutine->getScript();
		if (script->isStepping() && now >= coroutine->getNextRun())
		{
			coroutine->setNextRun(now + script->getStepTime());
			executeCoroutine(coroutine, io);
		}
		coroutine = coroutine->getNext();
	}
}

void VM::executeCoroutine(Coroutine * coroutine, GPIO * io)
{
	currentCoroutine = coroutine;
	coroutine->restoreStack(stack);
	pc = coroutine->getPC();
	currentScript = coroutine->getActiveScript();
	framePointer = coroutine->getFramePointer();
	if (framePointer == -1)
	{
		// TODO(Richo): This means we need to initialize the stack frame
		framePointer = stack->getPointer();
		stack->push(0); // Return value slot (default: 0)
		stack->push(uint32_to_float((uint32)framePointer << 16 | pc));
	}
	bool yieldFlag = false;
	while (true)
	{
		if (pc > currentScript->getInstructionStop())
		{
			unwindStackAndReturn();
			if (currentScript == coroutine->getScript())
			{
				/*
				INFO(Richo):
				If we get here it means we weren't called from other script, we just reached 
				the end of the script after a regular tick. We don't have to return any value. 
				We simply reset the coroutine state and break out of the loop.
				*/
				coroutine->setActiveScript(currentScript);
				coroutine->setFramePointer(-1);
				coroutine->setPC(currentScript->getInstructionStart());
				coroutine->saveStack(stack);
				break;
			}
			else
			{
				currentScript = currentProgram->getScriptForPC(pc);
			}
		}
		int8 breakCount = coroutine->getBreakCount();
		if (breakCount >= 0)
		{
			if (breakCount == 0)
			{
				coroutine->setActiveScript(currentScript);
				coroutine->setFramePointer(framePointer);
				coroutine->setPC(pc);
				coroutine->saveStack(stack);
				coroutine->setNextRun(millis());
				break;
			}
			coroutine->setBreakCount(breakCount - 1);
		}
		Instruction next = nextInstruction();
		executeInstruction(next, io, yieldFlag);
		if (stack->hasError())
		{
			// TODO(Richo): Notify client of stack error
			break;
		}
		if (yieldFlag)
		{
			coroutine->setActiveScript(currentScript);
			coroutine->setFramePointer(framePointer);
			coroutine->setPC(pc);
			coroutine->saveStack(stack);
			break;
		}
	}
}

Instruction VM::nextInstruction(void)
{
	return currentScript->getInstructionAt(pc++);
}

void VM::executeInstruction(Instruction instruction, GPIO * io, bool& yieldFlag)
{
	Opcode opcode = instruction.opcode;
	int16 argument = instruction.argument;
	switch (opcode)
	{
		case TURN_ON:
		{
			io->setValue((uint8)argument, HIGH);
		} 
		break;

		case TURN_OFF:
		{
			io->setValue((uint8)argument, LOW);
		} 
		break;

		case WRITE_PIN:
		{
			io->setValue((uint8)argument, stack->pop());
		} 
		break;

		case READ_PIN:
		{
			stack->push(io->getValue((uint8)argument));
		} 
		break;

		case READ_GLOBAL:
		{
			stack->push(currentProgram->getGlobal(argument));
		} 
		break;

		case WRITE_GLOBAL:
		{
			currentProgram->setGlobal(argument, stack->pop());
		} 
		break;
		
		case SCRIPT_CALL:
		{
			framePointer = stack->getPointer();
			stack->push(0); // Return value slot (default: 0)
			stack->push(uint32_to_float((uint32)framePointer << 16 | pc));
			currentScript = currentProgram->getScript(argument);
			pc = currentScript->getInstructionStart();
		} 
		break;

		case SCRIPT_START:
		{
			Script* script = currentProgram->getScript(argument);
			if (script != 0)
			{
				script->setStepping(true);
			}
		} 
		break;

		case SCRIPT_STOP:
		{
			Script* script = currentProgram->getScript(argument);
			if (script != 0)
			{
				script->setStepping(false);
			}
		} 
		break;

		case JMP:
		{
			pc += argument;
			if (argument < 0) { yieldTime(0, yieldFlag); }
		} 
		break;

		case JZ:
		{
			if (stack->pop() == 0) // TODO(Richo): Float comparison
			{
				pc += argument;
				if (argument < 0) { yieldTime(0, yieldFlag); }
			}
		} 
		break;

		case JNZ:
		{
			if (stack->pop() != 0) // TODO(Richo): Float comparison
			{
				pc += argument;
				if (argument < 0) { yieldTime(0, yieldFlag); }
			}
		} 
		break;

		case JNE:
		{
			float a = stack->pop();
			float b = stack->pop();
			if (a != b) // TODO(Richo): float comparison
			{
				pc += argument;
				if (argument < 0) { yieldTime(0, yieldFlag); }
			}
		} 
		break;

		case JLT:
		{
			float b = stack->pop();
			float a = stack->pop();
			if (a < b)
			{
				pc += argument;
				if (argument < 0) { yieldTime(0, yieldFlag); }
			}
		} 
		break;

		case JLTE:
		{
			float b = stack->pop();
			float a = stack->pop();
			if (a <= b)
			{
				pc += argument;
				if (argument < 0) { yieldTime(0, yieldFlag); }
			}
		} 
		break;

		case JGT:
		{
			float b = stack->pop();
			float a = stack->pop();
			if (a > b)
			{
				pc += argument;
				if (argument < 0) { yieldTime(0, yieldFlag); }
			}
		} 
		break;

		case JGTE:
		{
			float b = stack->pop();
			float a = stack->pop();
			if (a >= b)
			{
				pc += argument;
				if (argument < 0) { yieldTime(0, yieldFlag); }
			}
		} 
		break;

		case READ_LOCAL:
		{
			uint16 index = framePointer + argument;
			float value = stack->getElementAt(index);
			stack->push(value);
		} 
		break;

		case WRITE_LOCAL:
		{
			uint16 index = framePointer + argument;
			float value = stack->pop();
			stack->setElementAt(index, value);
		}
		break;

		case PRIM_READ_PIN:
		{
			uint8 pin = (uint8)stack->pop();
			stack->push(io->getValue(pin));
		}
		break;

		case PRIM_WRITE_PIN:
		{
			float value = stack->pop();
			uint8 pin = (uint8)stack->pop();
			io->setValue(pin, value);
		}
		break;

		case PRIM_TOGGLE_PIN:
		{
			uint8 pin = (uint8)stack->pop();
			io->setValue(pin, 1 - io->getValue(pin));
		}
		break;

		case PRIM_SERVO_DEGREES:
		{
			float value = stack->pop() / 180.0f;
			uint8 pin = (uint8)stack->pop();
			io->servoWrite(pin, value);
		}
		break;

		case PRIM_SERVO_WRITE:
		{
			float value = stack->pop();
			uint8 pin = (uint8)stack->pop();
			io->servoWrite(pin, value);
		}
		break;

		case PRIM_MULTIPLY:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 * val2);
		}
		break;

		case PRIM_ADD:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 + val2);
		}
		break;

		case PRIM_DIVIDE:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 / val2);
		}
		break;

		case PRIM_SUBTRACT:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 - val2);
		}
		break;

		case PRIM_SECONDS:
		{
			float time = (float)millis() / 1000.0;
			stack->push(time);
		}
		break;

		case PRIM_EQ:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 == val2); // TODO(Richo)
		}
		break;

		case PRIM_NEQ:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 != val2); // TODO(Richo)
		}
		break;

		case PRIM_GT:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 > val2);
		}
		break;

		case PRIM_GTEQ:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 >= val2);
		}
		break;

		case PRIM_LT:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 < val2);
		}
		break;

		case PRIM_LTEQ:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 <= val2);
		}
		break;

		case PRIM_NEGATE:
		{
			float val = stack->pop();
			stack->push(-1 * val);
		}
		break;

		case PRIM_SIN:
		{
			float val = stack->pop();
			stack->push(sinf(val));
		} 
		break;

		case PRIM_COS:
		{
			float val = stack->pop();
			stack->push(cosf(val));
		}
		break;

		case PRIM_TAN:
		{
			float val = stack->pop();
			stack->push(tanf(val));
		}
		break;

		case PRIM_TURN_ON:
		{
			uint8 pin = (uint8)stack->pop();
			io->setValue(pin, 1);
		}
		break;

		case PRIM_TURN_OFF:
		{
			uint8 pin = (uint8)stack->pop();
			io->setValue(pin, 0);
		}
		break;

		case PRIM_YIELD:
		{
			yieldTime(0, yieldFlag);
		}
		break;

		case PRIM_YIELD_TIME:
		{
			int32 time = (int32)stack->pop();
			yieldTime(time, yieldFlag);
		}
		break;

		case PRIM_MILLIS:
		{
			float time = (float)millis();
			stack->push(time);
		}
		break;

		case PRIM_RET:
		{
			if (currentScript != currentCoroutine->getScript())
			{
				unwindStackAndReturn();
				currentScript = currentProgram->getScriptForPC(pc);
			}
			else
			{
				/* 
				INFO(Richo): Jump pass the end of the script so that in the next iteration 
				the execution stops.
				*/
				pc = currentScript->getInstructionStop() + 1;
			}
		}
		break;

		case PRIM_POP:
		{
			// Throw value away
			stack->pop();
		}
		break;
	}

}

void VM::yieldTime(int32 time, bool& yieldFlag)
{
	currentCoroutine->setNextRun(millis() + time);
	yieldFlag = true;
}

void VM::unwindStackAndReturn(void)
{	
	uint32 value = float_to_uint32(stack->pop());
	pc = value & 0xFFFF;
	framePointer = value >> 16;

	float returnValue = stack->pop();
	// TODO(Richo): Pop args/locals

	// INFO(Richo): Only push a return value if we were called from other script
	if (currentScript != currentCoroutine->getScript())
	{
		stack->push(returnValue);
	}
}