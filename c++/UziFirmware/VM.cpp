#include "VM.h"

void VM::executeProgram(Program * program, GPIO * pe)
{
	int count = program->getScriptCount();
	Script * script = program->getScript();
	for (int i = 0; i < count; i++)
	{
		executeScript(script, pe);
		script = script->getNext();
	}
}

void VM::executeScript(Script * script, GPIO * pe)
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

	_ip = 0;
	_currentScript = script;
	_stack->reset();
	_pe = pe;
	unsigned char next = nextBytecode();
	while (next != (unsigned char)0xFF)
	{
		executeBytecode(next);
		if (_stack->overflow())
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
	return _currentScript->bytecodeAt(_ip++);
}

void VM::executeBytecode(unsigned char bytecode)
{
	unsigned char key = bytecode & 0xF0;
	unsigned char value = bytecode & 0x0F;

	switch (key)
	{
		case 0x00:
		{// pushLit
			_stack->push((float)_currentScript->literalAt(value));
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
			executePrimitive(value);
		} break;
		case 0x60:
		{// scriptCall
		} break;
		case 0x70:
		{// pop
			for (int i = 0; i < value; i++)
			{
				_stack->pop();
			}
		} break;
		case 0x80:
		{// jmp
			_ip = _ip + value;
		} break;
		case 0x90:
		{// jne
			float a = _stack->pop();
			float b = _stack->pop();
			if (a != b)
			{
				_ip = _ip + value;
			}
		} break;
		case 0xA0:
		{// jnz
			if (_stack->pop() != 0)
			{
				_ip = _ip + value;
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

void VM::executePrimitive(unsigned char primitiveIndex)
{

	switch (primitiveIndex)
	{
		case 0x00:
		{// getValue
			unsigned int pin = (unsigned int)_stack->pop();
			_stack->push(_pe->getValue(pin));
		} break;
		case 0x01:
		{// setValue
			float value = _stack->pop();
			unsigned int pin = (unsigned int)_stack->pop();
			_pe->setValue(pin, value);
		} break;
		case 0x02:
		{// toggle
			unsigned int pin = (unsigned int)_stack->pop();
			_pe->setValue(pin, 1 - _pe->getValue(pin));
		} break;
		case 0x03:
		{// getMode
			unsigned int pin = (unsigned int)_stack->pop();
			_stack->push(_pe->getMode(pin));
		} break;
		case 0x04:
		{// setMode
			unsigned char mode = (unsigned char)_stack->pop();
			unsigned int pin = (unsigned int)_stack->pop();
			_pe->setMode(pin, mode);
		} break;
		case 0x05:
		{// delay
			delay((unsigned long)_stack->pop());
		} break;
	}
}
