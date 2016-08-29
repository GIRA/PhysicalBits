#include "VM.h"

void VM::executeProgram(Program * program, GPIO * io)
{
	currentProgram = program;
	int16 count = program->getScriptCount();
	Script * script = program->getScript();
	for (int16 i = 0; i < count; i++)
	{
		executeScript(script, io);
		script = script->getNext();
	}
}

void VM::executeScript(Script * script, GPIO * io)
{
	if (!script->isStepping())
	{
		return;
	}
	int32 now = millis();
	if (!script->shouldStepNow(now))
	{
		return;
	}
	script->rememberLastStepTime(now);

	pc = 0;
	currentScript = script;
	stack->reset();
	while (pc < currentScript->getInstructionCount())
	{
		Instruction next = nextInstruction();
		executeInstruction(next, io);
		if (stack->overflow())
		{
			// TODO(Richo): Notify client of stack overflow
			break;
		}
	}	
}

Instruction VM::nextInstruction(void)
{
	return currentScript->getInstructionAt(pc++);
}

void VM::executeInstruction(Instruction instruction, GPIO * io)
{
	uint8 opcode = instruction.opcode;
	uint16 argument = instruction.argument;
	switch (opcode)
	{
		// Turn ON
		case 0x00:
		{
			io->setValue((uint8)argument, HIGH);
		} break;

		// Turn OFF
		case 0x01:
		{
			io->setValue((uint8)argument, LOW);
		} break;

		// Write
		case 0x02:
		{
			io->setValue((uint8)argument, stack->pop());
		} break;

		// Read
		case 0x03:
		{
			stack->push(io->getValue((uint8)argument));
		} break;

		// Push
		case 0xF8:
		case 0x08:
		{
			stack->push(currentProgram->getGlobal(argument));
		} break;

		// Pop
		case 0xF9:
		case 0x09:
		{
			currentProgram->setGlobal(argument, stack->pop());
		} break;

		// Prim call
		case 0xFB: argument += 256; // 288 -> 543
		case 0xFA: argument += 16;  // 32 -> 287
		case 0x0B: argument += 16;  // 16 -> 31
		case 0x0A:					// 0 -> 15
		{
			executePrimitive(argument, io);
		} break;

		// Script call
		case 0xFC:
		case 0x0C:
		{

		} break;

		// Start script
		case 0xFD:
		case 0x0D:
		{

		} break;

		// Stop script
		case 0xFE:
		case 0x0E:
		{

		} break;

		// Yield
		case 0xF0:
		{

		} break;

		// Yield time
		case 0xF1:
		{
		} break;

		// JNZ
		case 0xF2:
		{
			if (stack->pop() != 0)
			{
				pc += argument;
			}
		} break;

		// JNE
		case 0xF3:
		{
			float a = stack->pop();
			float b = stack->pop();
			if (a != b) // TODO(Richo): float comparison
			{
				pc += argument;
			}
		} break;

		// JLT
		case 0xF4:
		{

		} break;

		// JLTE
		case 0xF5:
		{

		} break;

		// JGT
		case 0xF6:
		{

		} break;

		// JGTE
		case 0xF7:
		{

		} break;

		// JMP
		case 0xFF:
		{
			pc += argument;
		} break;
	}

}

void VM::executePrimitive(uint16 primitiveIndex, GPIO * io)
{
	switch (primitiveIndex)
	{
		case 0x00:
		{// read
			uint8 pin = (uint8)stack->pop();
			stack->push(io->getValue(pin));
		} break;
		case 0x01:
		{// write
			float value = stack->pop();
			uint8 pin = (uint8)stack->pop();
			io->setValue(pin, value);
		} break;
		case 0x02:
		{// toggle
			uint8 pin = (uint8)stack->pop();
			io->setValue(pin, 1 - io->getValue(pin));
		} break;
		case 0x03:
		{// getMode
			uint8 pin = (uint8)stack->pop();
			stack->push(io->getMode(pin));
		} break;
		case 0x04:
		{// setMode
			uint8 mode = (uint8)stack->pop();
			uint8 pin = (uint8)stack->pop();
			io->setMode(pin, mode);
		} break;
		case 0x05:
		{// servoWrite
			float value = stack->pop();
			uint8 pin = (uint8)stack->pop();
			io->servoWrite(pin, value);
		} break;
	}
}
