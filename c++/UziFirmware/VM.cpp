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
	currentScript = currentProgram->getScriptForPC(pc);
	framePointer = coroutine->getFramePointer();
	if (framePointer == -1)
	{
		// TODO(Richo): This means we need to initialize the stack frame?
	}
	bool yieldFlag = false;
	while (true)
	{
		if (pc > currentScript->getInstructionStop())
		{
			if (currentScript == coroutine->getScript())
			{
				// INFO(Richo): The script was ticking and we reach the end
				coroutine->setFramePointer(-1);
				coroutine->setPC(currentScript->getInstructionStart());
				coroutine->saveStack(stack);
				break;
			}
			else
			{
				// INFO(Richo): The script was called from another thread, we must return a value
				uint32 value = float_to_uint32(stack->pop());
				pc = value & 0xFFFF;
				currentScript = currentProgram->getScriptForPC(pc);
			}
		}
		int8 breakCount = coroutine->getBreakCount();
		if (breakCount >= 0)
		{
			if (breakCount == 0)
			{
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
		if (stack->overflow())
		{
			// TODO(Richo): Notify client of stack overflow
			break;
		}
		if (yieldFlag)
		{
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
	uint8 opcode = instruction.opcode;
	int16 argument = instruction.argument;
	switch (opcode)
	{
		// Turn ON
		case 0x00:
		{
			io->setValue((uint8)argument, HIGH);
		} 
		break;

		// Turn OFF
		case 0x01:
		{
			io->setValue((uint8)argument, LOW);
		} 
		break;

		// Write
		case 0x02:
		{
			io->setValue((uint8)argument, stack->pop());
		} 
		break;

		// Read
		case 0x03:
		{
			stack->push(io->getValue((uint8)argument));
		} 
		break;

		// Push
		case 0xF8:
		case 0x08:
		{
			stack->push(currentProgram->getGlobal(argument));
		} 
		break;

		// Pop
		case 0xF9:
		case 0x09:
		{
			currentProgram->setGlobal(argument, stack->pop());
		} 
		break;

		// Prim call
		case 0xFB: argument += 256; // 288 -> 543
		case 0xFA: argument += 16;  // 32 -> 287
		case 0x0B: argument += 16;  // 16 -> 31
		case 0x0A:					// 0 -> 15
		{
			executePrimitive(argument, io, yieldFlag);
		} 
		break;

		// Script call
		case 0xFC:
		case 0x0C:
		{
			framePointer = stack->getPointer();
			stack->push(0); // Return value slot (default: 0)
			stack->push(uint32_to_float((uint32)framePointer << 16 | pc));
			currentScript = currentProgram->getScript(argument);
			pc = currentScript->getInstructionStart();
		} 
		break;

		// Start script
		case 0xFD:
		case 0x0D:
		{
			Script* script = currentProgram->getScript(argument);
			if (script != 0)
			{
				script->setStepping(true);
			}
		} 
		break;

		// Stop script
		case 0xFE:
		case 0x0E:
		{
			Script* script = currentProgram->getScript(argument);
			if (script != 0)
			{
				script->setStepping(false);
			}
		} 
		break;

		// JMP
		case 0xF0:
		{
			pc += argument;
			if (argument < 0) { yieldTime(0, yieldFlag); }
		} 
		break;

		// JZ
		case 0xF1:
		{
			if (stack->pop() == 0) // TODO(Richo): Float comparison
			{
				pc += argument;
				if (argument < 0) { yieldTime(0, yieldFlag); }
			}
		} 
		break;

		// JNZ
		case 0xF2:
		{
			if (stack->pop() != 0) // TODO(Richo): Float comparison
			{
				pc += argument;
				if (argument < 0) { yieldTime(0, yieldFlag); }
			}
		} 
		break;

		// JNE
		case 0xF3:
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

		// JLT
		case 0xF4:
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

		// JLTE
		case 0xF5:
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

		// JGT
		case 0xF6:
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

		// JGTE
		case 0xF7:
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

		// Read/Write Local
		case 0xFF:
		{
			// TODO(Richo): This instruction should read a value from the stack and copy it on top
		} 
		break;
	}

}

void VM::executePrimitive(uint16 primitiveIndex, GPIO * io, bool& yieldFlag)
{
	switch (primitiveIndex)
	{
		// read
		case 0x00:
		{
			uint8 pin = (uint8)stack->pop();
			stack->push(io->getValue(pin));
		} 
		break;

		// write
		case 0x01:
		{
			float value = stack->pop();
			uint8 pin = (uint8)stack->pop();
			io->setValue(pin, value);
		} 
		break;

		// toggle
		case 0x02:
		{
			uint8 pin = (uint8)stack->pop();
			io->setValue(pin, 1 - io->getValue(pin));
		}
		break;

		// servoDegrees
		case 0x03:
		{
			float value = stack->pop() / 180.0f;
			uint8 pin = (uint8)stack->pop();
			io->servoWrite(pin, value);
		}
		break;

		// servoWrite
		case 0x04:
		{
			float value = stack->pop();
			uint8 pin = (uint8)stack->pop();
			io->servoWrite(pin, value);
		} 
		break;

		// multiply
		case 0x05:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 * val2);
		} 
		break;

		// add
		case 0x06:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 + val2);
		} 
		break;

		// divide
		case 0x07:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 / val2);
		} 
		break;

		// subtract
		case 0x08:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 - val2);
		} 
		break;

		// seconds
		case 0x09:
		{
			float time = (float)millis() / 1000.0;
			stack->push(time);
		}
		break;
		
		// equals
		case 0x0A:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 == val2); // TODO(Richo)
		} 
		break;

		// notEquals
		case 0x0B:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 != val2); // TODO(Richo)
		} 
		break;

		// greaterThan
		case 0x0C:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 > val2);
		} 
		break;

		// greaterThanOrEquals
		case 0x0D:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 >= val2);
		} 
		break;

		// lessThan
		case 0x0E:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 < val2);
		} 
		break;

		// lessThanOrEquals
		case 0x0F:
		{
			float val2 = stack->pop();
			float val1 = stack->pop();
			stack->push(val1 <= val2);
		} 
		break;

		// negate
		case 0x10:
		{
			float val = stack->pop();
			stack->push(-1 * val);
		} 
		break;

		// sin
		case 0x11:
		{
			float val = stack->pop();
			stack->push(sinf(val));
		} break;

		// cos
		case 0x12:
		{
			float val = stack->pop();
			stack->push(cosf(val));
		} 
		break;

		// tan
		case 0x13:
		{
			float val = stack->pop();
			stack->push(tanf(val));
		} 
		break;

		// turnOn
		case 0x14:
		{
			uint8 pin = (uint8)stack->pop();
			io->setValue(pin, 1);
		} 
		break;

		// turnOff
		case 0x15:
		{
			uint8 pin = (uint8)stack->pop();
			io->setValue(pin, 0);
		} 
		break;

		// yield
		case 0x16:
		{
			yieldTime(0, yieldFlag);
		}
		break;

		// yieldTime
		case 0x17:
		{
			int32 time = (int32)stack->pop();
			yieldTime(time, yieldFlag);
		}
		break;

		// millis
		case 0x18:
		{
			float time = (float)millis();
			stack->push(time);
		}
		break;

		// ret
		case 0x19:
		{
			uint32 value = float_to_uint32(stack->pop());
			pc = value & 0xFFFF;
			currentScript = currentProgram->getScriptForPC(pc);
		}
		break;

		// pop
		case 0x1A:
		{
			float value = stack->pop();
		}
		break;

		// writeLocal:
	}
}

void VM::yieldTime(int32 time, bool& yieldFlag)
{
	currentCoroutine->setNextRun(millis() + time);
	yieldFlag = true;
}