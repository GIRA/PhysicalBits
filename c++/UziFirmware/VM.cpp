#include "VM.h"

Error VM::executeProgram(Program *program, GPIO *io, Monitor *monitor)
{
	if (program != currentProgram) 
	{
		currentProgram = program;
		currentCoroutine = 0;
	}

	int16 count = program->getScriptCount();

	lastTickStart = millis();
	for (int16 i = 0; i < count; i++)
	{
		Script* script = program->getScript(i);
		if (script->isStepping())
		{
			Coroutine* coroutine = script->getCoroutine();
			if (coroutine == 0)
			{
				return OUT_OF_MEMORY;
			}
			if (lastTickStart >= coroutine->getNextRun())
			{
				executeCoroutine(coroutine, io, monitor);
			}
		}
	}
	return NO_ERROR;
}

void VM::executeCoroutine(Coroutine *coroutine, GPIO *io, Monitor *monitor)
{
	if (this->halted
		|| (this->haltedScript != NULL && this->haltedScript != coroutine->getScript()))
	{
		/*
		INFO(Richo): Even though we won't execute this coroutine on this tick, I still
		adjust the last start so that when the VM continues the tasks are all in sync.
		*/
		coroutine->setLastStart(lastTickStart);
		return;
	}
	if (currentCoroutine != coroutine)
	{
		if (currentCoroutine != 0)
		{
			saveCurrentCoroutine();
		}
		currentCoroutine = coroutine;
		coroutine->restoreStack(&stack);
		pc = coroutine->getPC();
		currentScript = coroutine->getActiveScript();
		framePointer = coroutine->getFramePointer();
	}

	if (framePointer == -1)
	{
		framePointer = stack.getPointer();
		for (int i = 0; i < currentScript->getArgCount(); i++)
		{
			stack.push(0);
		}
		for (int i = 0; i < currentScript->getLocalCount(); i++)
		{
			stack.push(currentScript->getLocal(i));
		}
		stack.push(0); // Return value slot (default: 0)
		stack.push(uint32_to_float((uint32)-1 << 16 | pc));
		
		coroutine->setLastStart(lastTickStart);
	}
	bool yieldFlag = false;
	while (true)
	{
		if (pc <= currentScript->getInstructionStop())
		{
			Instruction next = currentScript->getInstructionAt(pc);
			if (getBreakpoint(&next) && this->haltedScript == NULL)
			{
				this->halted = true;
				this->haltedScript = coroutine->getScript();
				//this call is to ensure that the monitor has access to the updated state of the coroutine in the case of a halt.
				saveCurrentCoroutine();
				coroutine->setNextRun(lastTickStart);
				break;
			}
			this->haltedScript = NULL;
			pc++;
			executeInstruction(next, io, monitor, yieldFlag);
		}
		if (coroutine->hasError()) break;
		if (stack.hasError())
		{
			coroutine->setError(stack.getError());
			break;
		}
		if (yieldFlag)
		{
			break;
		}
		if (pc > currentScript->getInstructionStop())
		{
			bool returnFromScriptCall = framePointer != 0;
			unwindStackAndReturn();

			if (returnFromScriptCall)
			{
				currentScript = currentProgram->getScriptForPC(pc);
			}
			else
			{
				/*
				INFO(Richo):
				If we get here it means we weren't called from other script, we just reached
				the end of the script after a regular tick. We don't have to return any value.
				We simply reset the coroutine state and break out of the loop.
				*/

				framePointer = -1;
				pc = currentScript->getInstructionStart();
				coroutine->setNextRun(coroutine->getLastStart() + currentProgram->getGlobal(currentScript->interval));
				break;
			}
		}
	}
}

void VM::saveCurrentCoroutine()
{
	currentCoroutine->saveStack(&stack);
	currentCoroutine->setActiveScript(currentScript);
	currentCoroutine->setFramePointer(framePointer);
	currentCoroutine->setPC(pc);
}

void VM::executeInstruction(Instruction instruction, GPIO * io, Monitor *monitor, bool& yieldFlag)
{
	Opcode opcode = (Opcode)instruction.opcode;
	int16 argument = getArgument(&instruction);
          
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
			io->setValue((uint8)argument, stack.pop());
		}
		break;

		case READ_PIN:
		{
			stack.push(io->getValue((uint8)argument));
		}
		break;

		case READ_GLOBAL:
		{
			stack.push(currentProgram->getGlobal(argument));
		}
		break;

		case WRITE_GLOBAL:
		{
			currentProgram->setGlobal(argument, stack.pop());
		}
		break;

		case SCRIPT_CALL:
		{
			/*
			INFO(Richo):
			We know the arguments are already on the stack (it's the compiler's job
			to push them). Now we need to push:
				1) The local variables with their default values.
				2) The return value (default: 0)
				3) The current framePointer and returnAddress (so that when unwinding
				the stack, they can be set correctly).
			*/
			currentScript = currentProgram->getScript(argument);
			int16 fp = stack.getPointer() - currentScript->getArgCount();
			for (int i = 0; i < currentScript->getLocalCount(); i++)
			{
				stack.push(currentScript->getLocal(i));
			}
			stack.push(0); // Return value slot (default: 0)
			stack.push(uint32_to_float((uint32)framePointer << 16 | pc));

			/*
			INFO(Richo):
			After the stack is configured. We set the framePointer and pc to their
			new values and continue execution.
			*/
			framePointer = fp;
			pc = currentScript->getInstructionStart();
		}
		break;

		case SCRIPT_START:
		{
			Script* script = currentProgram->getScript(argument);
			if (script != 0)
			{
				script->setStepping(true);

				Coroutine* coroutine = script->getCoroutine();
				if (coroutine == 0)
				{
					currentCoroutine->setError(OUT_OF_MEMORY);
				}
				else if (currentCoroutine == coroutine)
				{
					/*
					If we're starting the current coroutine we need to restart execution
					right now. So, we set the yield flag and reset the vm state.
					*/
					yieldFlag = true;
					stack.reset();
					pc = script->getInstructionStart();
					framePointer = -1;
				}
				else
				{
					/*
					If we're starting another coroutine just resetting the coroutine
					state is enough.
					*/
					coroutine->reset();
				}
			}
		}
		break;

		case SCRIPT_RESUME:
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

				Coroutine* coroutine = script->getCoroutine();
				if (coroutine == 0)
				{
					currentCoroutine->setError(OUT_OF_MEMORY);
				}
				else if (currentCoroutine == coroutine)
				{
					/*
					If we're stopping the current coroutine we need to stop execution
					right now. So, we set the yield flag and reset the vm state.
					*/
					yieldFlag = true;
					stack.reset();
					pc = script->getInstructionStart();
					framePointer = -1;
				}
				else
				{
					/*
					If we're stopping another coroutine just resetting the coroutine
					state is enough.
					*/
					coroutine->reset();
				}
			}
		}
		break;

		case SCRIPT_PAUSE:
		{
			Script* script = currentProgram->getScript(argument);
			if (script != 0)
			{
				script->setStepping(false);

				/*
				If we're stopping the current coroutine we need to stop execution
				right now. But we don't need to reset the coroutine because we will
				resume execution from this point.
				*/
				Coroutine* coroutine = script->getCoroutine();
				if (coroutine == 0)
				{
					currentCoroutine->setError(OUT_OF_MEMORY);
				}
				else if (currentCoroutine == coroutine)
				{
					yieldFlag = true;
				}
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
			if (stack.pop() == 0) // TODO(Richo): Float comparison
			{
				pc += argument;
				if (argument < 0) { yieldTime(0, yieldFlag); }
			}
		}
		break;

		case JNZ:
		{
			if (stack.pop() != 0) // TODO(Richo): Float comparison
			{
				pc += argument;
				if (argument < 0) { yieldTime(0, yieldFlag); }
			}
		}
		break;

		case JNE:
		{
			float a = stack.pop();
			float b = stack.pop();
			if (a != b) // TODO(Richo): float comparison
			{
				pc += argument;
				if (argument < 0) { yieldTime(0, yieldFlag); }
			}
		}
		break;

		case JLT:
		{
			float b = stack.pop();
			float a = stack.pop();
			if (a < b)
			{
				pc += argument;
				if (argument < 0) { yieldTime(0, yieldFlag); }
			}
		}
		break;

		case JLTE:
		{
			float b = stack.pop();
			float a = stack.pop();
			if (a <= b)
			{
				pc += argument;
				if (argument < 0) { yieldTime(0, yieldFlag); }
			}
		}
		break;

		case JGT:
		{
			float b = stack.pop();
			float a = stack.pop();
			if (a > b)
			{
				pc += argument;
				if (argument < 0) { yieldTime(0, yieldFlag); }
			}
		}
		break;

		case JGTE:
		{
			float b = stack.pop();
			float a = stack.pop();
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
			float value = stack.getElementAt(index);
			stack.push(value);
		}
		break;

		case WRITE_LOCAL:
		{
			uint16 index = framePointer + argument;
			float value = stack.pop();
			stack.setElementAt(index, value);
		}
		break;

		case PRIM_READ_PIN:
		{
			uint8 pin = (uint8)stack.pop();
			stack.push(io->getValue(pin));
		}
		break;

		case PRIM_WRITE_PIN:
		{
			float value = stack.pop();
			uint8 pin = (uint8)stack.pop();
			io->setValue(pin, value);
		}
		break;

		case PRIM_TOGGLE_PIN:
		{
			uint8 pin = (uint8)stack.pop();
			io->setMode(pin, OUTPUT);
			io->setValue(pin, 1 - io->getValue(pin));
		}
		break;

		case PRIM_SERVO_DEGREES:
		{
			float value = stack.pop() / 180.0f;
			uint8 pin = (uint8)stack.pop();
			io->servoWrite(pin, value);
		}
		break;

		case PRIM_SERVO_WRITE:
		{
			float value = stack.pop();
			uint8 pin = (uint8)stack.pop();
			io->servoWrite(pin, value);
		}
		break;

		case PRIM_MULTIPLY:
		{
			float val2 = stack.pop();
			float val1 = stack.pop();
			stack.push(val1 * val2);
		}
		break;

		case PRIM_ADD:
		{
			float val2 = stack.pop();
			float val1 = stack.pop();
			stack.push(val1 + val2);
		}
		break;

		case PRIM_DIVIDE:
		{
			float val2 = stack.pop();
			float val1 = stack.pop();
			stack.push(val1 / val2);
		}
		break;

		case PRIM_SUBTRACT:
		{
			float val2 = stack.pop();
			float val1 = stack.pop();
			stack.push(val1 - val2);
		}
		break;

		case PRIM_SECONDS:
		{
			float time = (float)millis() / 1000.0;
			stack.push(time);
		}
		break;

		case PRIM_MINUTES:
		{
			float time = (float)millis() / 1000.0 / 60.0;
			stack.push(time);
		}
		break;

		case PRIM_EQ:
		{
			float val2 = stack.pop();
			float val1 = stack.pop();
			stack.push(val1 == val2); // TODO(Richo)
		}
		break;

		case PRIM_NEQ:
		{
			float val2 = stack.pop();
			float val1 = stack.pop();
			stack.push(val1 != val2); // TODO(Richo)
		}
		break;

		case PRIM_GT:
		{
			float val2 = stack.pop();
			float val1 = stack.pop();
			stack.push(val1 > val2);
		}
		break;

		case PRIM_GTEQ:
		{
			float val2 = stack.pop();
			float val1 = stack.pop();
			stack.push(val1 >= val2);
		}
		break;

		case PRIM_LT:
		{
			float val2 = stack.pop();
			float val1 = stack.pop();
			stack.push(val1 < val2);
		}
		break;

		case PRIM_LTEQ:
		{
			float val2 = stack.pop();
			float val1 = stack.pop();
			stack.push(val1 <= val2);
		}
		break;

		case PRIM_NEGATE:
		{
			float val = stack.pop();
			stack.push(val == 0 ? 1 : 0);
		}
		break;

		case PRIM_SIN:
		{
			float val = stack.pop();
			stack.push(sinf(val));
		}
		break;

		case PRIM_COS:
		{
			float val = stack.pop();
			stack.push(cosf(val));
		}
		break;

		case PRIM_TAN:
		{
			float val = stack.pop();
			stack.push(tanf(val));
		}
		break;

		case PRIM_TURN_ON:
		{
			uint8 pin = (uint8)stack.pop();
			io->setValue(pin, 1);
		}
		break;

		case PRIM_TURN_OFF:
		{
			uint8 pin = (uint8)stack.pop();
			io->setValue(pin, 0);
		}
		break;

		case PRIM_YIELD:
		{
			yieldTime(0, yieldFlag);
		}
		break;

		case PRIM_DELAY_MILLIS:
		{
			int32 time = (int32)stack.pop();
			yieldTime(time, yieldFlag);
		}
		break;

		case PRIM_DELAY_SECONDS:
		{
			float seconds = stack.pop();
			int32 time = seconds * 1000;
			yieldTime(time, yieldFlag);
		}
		break;

		case PRIM_DELAY_MINUTES:
		{
			float minutes = stack.pop();
			int32 time = minutes * 60 * 1000;
			yieldTime(time, yieldFlag);
		}
		break;

		case PRIM_MILLIS:
		{
			float time = (float)millis();
			stack.push(time);
		}
		break;

		case PRIM_RET:
		{
			bool returnFromScriptCall = framePointer != 0;
			if (returnFromScriptCall)
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
			stack.pop();
		}
		break;

		case PRIM_RETV:
		{
			uint16 index = framePointer +
				currentScript->getArgCount() +
				currentScript->getLocalCount();
			// TODO(Richo): Duplicated code from WRITE_LOCAL 
			float value = stack.pop();
			stack.setElementAt(index, value);

			// TODO(Richo): Duplicated code from PRIM_RET
			bool returnFromScriptCall = framePointer != 0;
			if (returnFromScriptCall)
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

		case PRIM_COROUTINE:
		{
			stack.push(currentCoroutine->getScript()->getIndex());
		}
		break;

		case PRIM_LOGICAL_AND:
		{
			float a = stack.pop();
			float b = stack.pop();
			stack.push(a && b);
		}
		break;

		case PRIM_LOGICAL_OR:
		{
			float a = stack.pop();
			float b = stack.pop();
			stack.push(a || b);
		}
		break;

		case PRIM_BITWISE_AND:
		{
			uint32 a = (uint32)stack.pop();
			uint32 b = (uint32)stack.pop();
			stack.push(a & b);
		}
		break;

		case PRIM_BITWISE_OR:
		{
			uint32 a = (uint32)stack.pop();
			uint32 b = (uint32)stack.pop();
			stack.push(a | b);
		}
		break;

		case PRIM_SERIAL_WRITE:
		{
			uint8 a = stack.pop();
			monitor->serialWrite(a);
		}
		break;

		case PRIM_ROUND:
		{
			float a = stack.pop();
			stack.push(round(a));
		}
		break;

		case PRIM_CEIL:
		{
			float a = stack.pop();
			stack.push(ceil(a));
		}
		break;

		case PRIM_FLOOR:
		{
			float a = stack.pop();
			stack.push(floor(a));
		}
		break;

		case PRIM_SQRT:
		{
			float a = stack.pop();
			stack.push(sqrt(a));
		}
		break;

		case PRIM_ABS:
		{
			float a = stack.pop();
			stack.push(fabs(a));
		}
		break;

		case PRIM_LN:
		{
			float a = stack.pop();
			stack.push(log(a));
		}
		break;

		case PRIM_LOG10:
		{
			float a = stack.pop();
			stack.push(log10f(a));
		}
		break;

		case PRIM_EXP:
		{
			float a = stack.pop();
			stack.push(expf(a));
		}
		break;

		case PRIM_POW10:
		{
			float a = stack.pop();
			stack.push(powf(10, a));
		}
		break;

		case PRIM_ASIN:
		{
			float a = stack.pop();
			stack.push(asinf(a));
		}
		break;

		case PRIM_ACOS:
		{
			float a = stack.pop();
			stack.push(acosf(a));
		}
		break;

		case PRIM_ATAN:
		{
			float a = stack.pop();
			stack.push(atan(a));
		}
		break;

		case PRIM_POWER:
		{
			float b = stack.pop();
			float a = stack.pop();
			stack.push(pow(a, b));
		}
		break;

		case PRIM_IS_ON:
		{
			uint8 pin = (uint8)stack.pop();
			stack.push(io->getValue(pin) > 0);
		}
		break;

		case PRIM_IS_OFF:
		{
			uint8 pin = (uint8)stack.pop();
			stack.push(io->getValue(pin) == 0);
		}
		break;

		case PRIM_MOD:
		{
			float b = stack.pop();
			float a = stack.pop();
			stack.push(fmod(a, b));
		}
		break;

		case PRIM_CONSTRAIN:
		{
			float c = stack.pop();
			float b = stack.pop();
			float a = stack.pop();
			if (a < b)
			{
				stack.push(b);
			}
			else if (a > c)
			{
				stack.push(c);
			}
			else
			{
				stack.push(a);
			}
		}
		break;

		case PRIM_RANDOM_INT:
		{
			int32 b = (int32)stack.pop();
			int32 a = (int32)stack.pop();
			if (b > a)
			{
				stack.push(random(a, b));
			}
			else
			{
				stack.push(random(b, a));
			}
		}
		break;

		case PRIM_RANDOM:
		{
			int32 max = 0x7FFFFFFF;
			int32 r1 = fmod(random(max), max);
			float r2 = (float)((double)r1 / (double)max);
			stack.push(r2);
		}
		break;

		case PRIM_IS_EVEN:
		{
			int32 a = (int32)stack.pop();
			stack.push(a % 2 == 0 ? 1 : 0);
		}
		break;

		case PRIM_IS_ODD:
		{
			int32 a = (int32)stack.pop();
			stack.push(a % 2 == 0 ? 0 : 1);
		}
		break;

		case PRIM_IS_PRIME:
		{
			int32 a = (int32)stack.pop();
			if (a <= 1) { stack.push(0); }
			else if (a % 2 == 0) { stack.push(a == 2 ? 1 : 0); }
			else
			{
				bool result = true;
				for (int32 i = 3; i <= sqrt(a); i += 2)
				{
					if (a % i == 0)
					{
						result = false;
						break;
					}
				}
				stack.push(result ? 1 : 0);
			}
		}
		break;

		case PRIM_IS_WHOLE:
		{
			float a = stack.pop();
			int32 a_int = (int32)a;
			stack.push(a == a_int ? 1 : 0);
		}
		break;

		case PRIM_IS_POSITIVE:
		{
			float a = stack.pop();
			stack.push(a >= 0 ? 1 : 0);
		}
		break;

		case PRIM_IS_NEGATIVE:
		{
			float a = stack.pop();
			stack.push(a < 0 ? 1 : 0);
		}
		break;

		case PRIM_IS_DIVISIBLE_BY:
		{
			float b = stack.pop();
			float a = stack.pop();
			if (b == 0) { stack.push(0); }
			else if (b != (int32)b) { stack.push(0); }
			else
			{
				stack.push(fmod(a, b) == 0 ? 1 : 0);
			}
		}
		break;

		case PRIM_IS_CLOSE_TO:
		{
			float epsilon = 0.0001;
			float b = stack.pop();
			float a = stack.pop();
			if (a == 0)
			{
				stack.push(b < epsilon ? 1 : 0);
			}
			else if (b == 0)
			{
				stack.push(a < epsilon ? 1 : 0);
			}
			else if (a == b)
			{
				stack.push(1);
			}
			else
			{
				float a_abs = fabs(a);
				float b_abs = fabs(b);
				float max = a_abs > b_abs ? a_abs : b_abs;
				float diff = fabs(a - b);
				stack.push(diff / max < epsilon ? 1 : 0);
			}
		}
		break;

		case PRIM_SONAR_DIST_CM:
		{
			unsigned int maxDist = stack.pop();
			uint8 echoPin = stack.pop();
			uint8 trigPin = stack.pop();

			NewPing sonar(trigPin, echoPin, maxDist);
			unsigned long dist = sonar.ping_cm();
			if (dist > 0) 
			{
				stack.push(dist);
			} 
			else 
			{
				stack.push(INFINITY);
			}
		}
		break;

		case PRIM_MATRIX_8x8_DISPLAY:
		{
			uint8 pins_x[8];
			uint8 pins_y[8];
			uint8 rows[8];

			for (int8 i = 7; i >= 0; i--)
			{
				rows[i] = (uint8)stack.pop();
			}
			for (int8 i = 7; i >= 0; i--)
			{
				pins_y[i] = (uint8)stack.pop();
			}
			for (int8 i = 7; i >= 0; i--)
			{
				pins_x[i] = (uint8)stack.pop();
			}

			for (int8 i = 0; i < 8; i++)
			{
				io->setValue(pins_y[i], LOW);
				for (int j = 0; j < 8; j++)
				{
					io->setValue(pins_x[7 - j], (rows[i] >> j) & 1);
				}

				delayMicroseconds(100);

				for (int j = 0; j < 8; j++)
				{
					io->setValue(pins_x[j], LOW);
				}
				io->setValue(pins_y[i], HIGH);
			}
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
	bool returnFromScriptCall = framePointer != 0;
	uint32 value = float_to_uint32(stack.pop());
	pc = value & 0xFFFF;
	framePointer = value >> 16;

	float returnValue = stack.pop();

	// INFO(Richo): Pop args/locals
	int varCount = currentScript->getArgCount() + currentScript->getLocalCount();
	stack.discard(varCount);


	// INFO(Richo): Only push a return value if we were called from another script
	if (returnFromScriptCall)
	{
		stack.push(returnValue);
	}
}

void VM::reset()
{
	halted = false;
	haltedScript = 0;
	framePointer = 0;
	pc = 0;
	currentProgram = 0;
	currentCoroutine = 0;
	currentScript = 0;
}
