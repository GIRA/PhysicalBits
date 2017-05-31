#pragma once

#include "Reader.h"

enum Opcode
{
	TURN_ON, TURN_OFF, WRITE_PIN, READ_PIN,
	READ_GLOBAL, WRITE_GLOBAL, 
	SCRIPT_CALL, SCRIPT_START, SCRIPT_STOP,
	JMP, JZ, JNZ, JNE, JLT, JLTE, JGT, JGTE,
	READ_LOCAL, WRITE_LOCAL,

	PRIM_READ_PIN, PRIM_WRITE_PIN, PRIM_TOGGLE_PIN,
	PRIM_SERVO_DEGREES, PRIM_SERVO_WRITE,
	PRIM_MULTIPLY, PRIM_ADD, PRIM_DIVIDE, PRIM_SUBTRACT,
	PRIM_SECONDS, PRIM_MILLIS,
	PRIM_EQ, PRIM_NEQ, PRIM_GT, PRIM_GTEQ, PRIM_LT, PRIM_LTEQ,
	PRIM_NEGATE,
	PRIM_SIN, PRIM_COS, PRIM_TAN,
	PRIM_TURN_ON, PRIM_TURN_OFF,
	PRIM_YIELD, PRIM_YIELD_TIME,
	PRIM_RET, PRIM_POP, PRIM_RETV,
	PRIM_COROUTINE,
	PRIM_LOGICAL_AND, PRIM_LOGICAL_OR,
	PRIM_BITWISE_AND, PRIM_BITWISE_OR

};

struct Instruction
{
	Opcode opcode;
	int16 argument;
};

Instruction* readInstructions(Reader* rs, uint8 count, bool& timeout);
