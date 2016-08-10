#include "VM.h"

void VM::executeProgram(Program * program, GPIO * io)
{
	int count = program->getScriptCount();
	Script * script = program->getScript();
	for (int i = 0; i < count; i++)
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
	long now = millis();
	if (!script->shouldStepNow(now))
	{
		return;
	}
	script->rememberLastStepTime(now);

	pc = 0;
	currentScript = script;
	stack->reset();
	unsigned char next = nextBytecode();
	while (next != (unsigned char)0xFF)
	{
		executeBytecode(next, io);
		if (stack->overflow())
		{
			next = (unsigned char)0xFF;
		}
		else
		{
			next = nextBytecode();
		}
	}
}

unsigned char VM::nextBytecode(void)
{
	return currentScript->bytecodeAt(pc++);
}

void VM::executeBytecode(unsigned char bytecode, GPIO * io)
{
	unsigned char key = bytecode & 0xF0;
	unsigned char value = bytecode & 0x0F;

	switch (key)
	{
		case 0x00:
		{// pushLit
			stack->push((float)currentScript->literalAt(value));
		} break;
		case 0x10:
		{// pushLocal
		} break;
		case 0x20:
		{// pushGlobal
		} break;
		case 0x30:
		{// setLocal
		} break;
		case 0x40:
		{// setGlobal
		} break;
		case 0x50:
		{// primCall
			executePrimitive(value, io);
		} break;
		case 0x60:
		{// scriptCall
		} break;
		case 0x70:
		{// pop
			for (int i = 0; i < value; i++)
			{
				stack->pop();
			}
		} break;
		case 0x80:
		{// jmp
			pc = pc + value;
		} break;
		case 0x90:
		{// jne
			float a = stack->pop();
			float b = stack->pop();
			if (a != b)
			{
				pc = pc + value;
			}
		} break;
		case 0xA0:
		{// jnz
			if (stack->pop() != 0)
			{
				pc = pc + value;
			}
		} break;
		case 0xB0:
		{
		} break;
		case 0xC0:
		{
		} break;
		case 0xD0:
		{
		} break;
		case 0xE0:
		{
		} break;
		case 0xF0:
		{// extend
		} break;
	}
}

void VM::executePrimitive(unsigned char primitiveIndex, GPIO * io)
{

	switch (primitiveIndex)
	{
		case 0x00:
		{// getValue
			unsigned int pin = (unsigned int)stack->pop();
			stack->push(io->getValue(pin));
		} break;
		case 0x01:
		{// setValue
			float value = stack->pop();
			unsigned int pin = (unsigned int)stack->pop();
			io->setValue(pin, value);
		} break;
		case 0x02:
		{// toggle
			unsigned int pin = (unsigned int)stack->pop();
			io->setValue(pin, 1 - io->getValue(pin));
		} break;
		case 0x03:
		{// getMode
			unsigned int pin = (unsigned int)stack->pop();
			stack->push(io->getMode(pin));
		} break;
		case 0x04:
		{// setMode
			unsigned char mode = (unsigned char)stack->pop();
			unsigned int pin = (unsigned int)stack->pop();
			io->setMode(pin, mode);
		} break;
		case 0x05:
		{// delay
			delay((unsigned long)stack->pop());
		} break;
	}
}
