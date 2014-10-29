#include "StackVM.h"


void StackVM::executeProgram(StackProgram * program, PE * pe) {
	if (!program->isStepping()) {
		return;
	}
	long now = pe->getMillis();
	if (!program->shouldStepNow(now)) {
		return;
	}
	program->rememberLastStepTime(now);

	_ip = 0;
	_currentProgram = program;
	_stack->reset();
	_pe = pe;
	unsigned char next = nextBytecode();
	while(next != (unsigned char)0xFF) {
		executeBytecode(next);
		if (_stack->overflow()) {
			next = (unsigned char)0xFF;
		} else {
			next = nextBytecode();
		}
	}
}

unsigned char StackVM::nextBytecode(void) {
	unsigned char bytecode = _currentProgram->bytecodeAt(_ip);
	_ip++;
	return bytecode;
}

void StackVM::executeBytecode(unsigned char bytecode) {
	unsigned char key = bytecode & 0xF0;
	unsigned char value = bytecode & 0x0F;
	
	float a;
	float b;

	switch (key) {
		case 0x00:// pushLit
			_stack->push(_currentProgram->literalAt(value));
			break;
		case 0x10:// pushLocal
			break;
		case 0x20:// pushGlobal
			break;
		case 0x30:// setLocal
			break;
		case 0x40:// setGlobal
			break;
		case 0x50:// primCall
			executePrimitive(value);
			break;
		case 0x60:// scriptCall
			break;
		case 0x70:// pop
			for (int i = 0; i < value; i++) {
				_stack->pop();
			}
			break;		
		case 0x80:// jmp
			_ip = _ip + value;
			break;
		case 0x90:// jne
			a = _stack->pop();
			b = _stack->pop();
			if (a != b) {
				_ip = _ip + value;
			}
			break;
		case 0xA0:// jnz
			if (_stack->pop() != 0) {
				_ip = _ip + value;
			}
			break;
		case 0xB0:
			break;
		case 0xC0:
			break;
		case 0xD0:
			break;
		case 0xE0:
			break;
		case 0xF0:// extend
			break;
	}
}

void StackVM::executePrimitive(unsigned char primitiveIndex) {

	float pop1;
	float pop2;

	switch(primitiveIndex) {
		case 0x00:// getValue
			pop1 = _stack->pop();
			_stack->push(_pe->getValue((unsigned int)pop1));
			break;
		case 0x01:// setValue
			pop1 = _stack->pop();
			pop2 = _stack->pop(); 
			_pe->setValue((unsigned int)pop2, pop1);
			break;
		case 0x02:// getMode
			pop1 = _stack->pop();
			_stack->push(_pe->getMode((unsigned int)pop1));
			break;
		case 0x03:// setMode
			pop1 = _stack->pop();
			pop2 = _stack->pop();
			_pe->setMode((unsigned int)pop2, (unsigned char)pop1);
			break;
		case 0x04:// delay
			_pe->delayMs(_stack->pop());
			break;
	}
}
