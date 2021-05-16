#include "Instruction.h"

#define BREAKPOINT_MASK 0b10000000

void readInstruction(Reader* rs, Instruction* instruction, bool& timeout)
{
	uint8 bytecode = rs->next(timeout);
	if (timeout) return;

	uint8 opcode;
	int16 argument;
	if (bytecode < 0x80)
	{
		/*
		If the high-order bit is zero (< 0x8) then the opcode is stored in the 3 msbits
		and the argument is stored in the 5 lsbits.
		*/
		opcode = bytecode >> 5;
		argument = bytecode & 0x1F;
	}
	else
	{
		/*
		If the high-order bit is one (>= 0x8) then the opcode is stored in the 4 msbits
		and the argument is stored in the 4 lsbits.
		*/
		opcode = bytecode >> 4;
		argument = bytecode & 0xF;
		if (0xF == opcode)
		{
			/*
			Special case: If the 4 msbits happen to be 0xF then the argument is stored
			on the next byte.
			*/
			opcode = bytecode;
			argument = rs->next(timeout);
			if (timeout) return;

			/*
			If the opcode is one of the "jump" instructions, the argument is encoded
			using two's complement.
			*/
			if (opcode >= 0xF0 && opcode <= 0xF7 && argument >= 128)
			{
				argument = (0xFF & ((argument ^ 0xFF) + 1)) * -1;
			}
		}
	}

	// Now we assign the actual opcode and argument
	switch (opcode)
	{
		case 0x00: instruction->opcode = TURN_ON; break;
		case 0x01: instruction->opcode = TURN_OFF; break;
		case 0x02: instruction->opcode = WRITE_PIN; break;
		case 0x03: instruction->opcode = READ_PIN; break;

		case 0xF0: instruction->opcode = JMP; break;
		case 0xF1: instruction->opcode = JZ; break;
		case 0xF2: instruction->opcode = JNZ; break;
		case 0xF3: instruction->opcode = JNE; break;
		case 0xF4: instruction->opcode = JLT; break;
		case 0xF5: instruction->opcode = JLTE; break;
		case 0xF6: instruction->opcode = JGT; break;
		case 0xF7: instruction->opcode = JGTE; break;

		case 0xFF:
		{
			instruction->opcode = argument >> 7 ? WRITE_LOCAL : READ_LOCAL;
			argument = argument & 0x7F;
		}
		break;

		case 0xF8:
		case 0x08:
			instruction->opcode = READ_GLOBAL;
			break;

		case 0xF9:
		case 0x09:
			instruction->opcode = WRITE_GLOBAL;
			break;

		case 0xFB: argument += 256; // 288 -> 543
		case 0xFA: argument += 16;  // 32 -> 287
		case 0x0B: argument += 16;  // 16 -> 31
		case 0x0A:					// 0 -> 15
		{
			switch (argument)
			{
				// TODO(Richo): Reorder this so that most-used primitives go first
				case 0x00: instruction->opcode = PRIM_READ_PIN; break;
				case 0x01: instruction->opcode = PRIM_WRITE_PIN; break;
				case 0x02: instruction->opcode = PRIM_TOGGLE_PIN; break;
				case 0x03: instruction->opcode = PRIM_SET_SERVO_DEGREES; break;
				case 0x04: instruction->opcode = PRIM_SERVO_WRITE; break;
				case 0x05: instruction->opcode = PRIM_MULTIPLY; break;
				case 0x06: instruction->opcode = PRIM_ADD; break;
				case 0x07: instruction->opcode = PRIM_DIVIDE; break;
				case 0x08: instruction->opcode = PRIM_SUBTRACT; break;
				case 0x09: instruction->opcode = PRIM_SECONDS; break;
				case 0x0A: instruction->opcode = PRIM_EQ; break;
				case 0x0B: instruction->opcode = PRIM_NEQ; break;
				case 0x0C: instruction->opcode = PRIM_GT; break;
				case 0x0D: instruction->opcode = PRIM_GTEQ; break;
				case 0x0E: instruction->opcode = PRIM_LT; break;
				case 0x0F: instruction->opcode = PRIM_LTEQ; break;
				case 0x10: instruction->opcode = PRIM_NEGATE; break;
				case 0x11: instruction->opcode = PRIM_SIN; break;
				case 0x12: instruction->opcode = PRIM_COS; break;
				case 0x13: instruction->opcode = PRIM_TAN; break;
				case 0x14: instruction->opcode = PRIM_TURN_ON; break;
				case 0x15: instruction->opcode = PRIM_TURN_OFF; break;
				case 0x16: instruction->opcode = PRIM_YIELD; break;
				case 0x17: instruction->opcode = PRIM_DELAY_MILLIS; break;
				case 0x18: instruction->opcode = PRIM_MILLIS; break;
				case 0x19: instruction->opcode = PRIM_RET; break;
				case 0x1A: instruction->opcode = PRIM_POP; break;
				case 0x1B: instruction->opcode = PRIM_RETV; break;
				case 0x1C: instruction->opcode = PRIM_COROUTINE; break;
				case 0x1D: instruction->opcode = PRIM_LOGICAL_AND; break;
				case 0x1E: instruction->opcode = PRIM_LOGICAL_OR; break;
				case 0x1F: instruction->opcode = PRIM_BITWISE_AND; break;
				case 0x20: instruction->opcode = PRIM_BITWISE_OR; break;
				case 0x21: instruction->opcode = PRIM_SERIAL_WRITE; break;
				case 0x22: instruction->opcode = PRIM_ROUND; break;
				case 0x23: instruction->opcode = PRIM_CEIL; break;
				case 0x24: instruction->opcode = PRIM_FLOOR; break;
				case 0x25: instruction->opcode = PRIM_SQRT; break;
				case 0x26: instruction->opcode = PRIM_ABS; break;
				case 0x27: instruction->opcode = PRIM_LN; break;
				case 0x28: instruction->opcode = PRIM_LOG10; break;
				case 0x29: instruction->opcode = PRIM_EXP; break;
				case 0x2A: instruction->opcode = PRIM_POW10; break;
				case 0x2B: instruction->opcode = PRIM_ASIN; break;
				case 0x2C: instruction->opcode = PRIM_ACOS; break;
				case 0x2D: instruction->opcode = PRIM_ATAN; break;
				case 0x2E: instruction->opcode = PRIM_POWER; break;
				case 0x2F: instruction->opcode = PRIM_IS_ON; break;
				case 0x30: instruction->opcode = PRIM_IS_OFF; break;
				case 0x31: instruction->opcode = PRIM_REMAINDER; break;
				case 0x32: instruction->opcode = PRIM_CONSTRAIN; break;
				case 0x33: instruction->opcode = PRIM_RANDOM_INT; break;
				case 0x34: instruction->opcode = PRIM_RANDOM; break;
				case 0x35: instruction->opcode = PRIM_IS_EVEN; break;
				case 0x36: instruction->opcode = PRIM_IS_ODD; break;
				case 0x37: instruction->opcode = PRIM_IS_PRIME; break;
				case 0x38: instruction->opcode = PRIM_IS_WHOLE; break;
				case 0x39: instruction->opcode = PRIM_IS_POSITIVE; break;
				case 0x3A: instruction->opcode = PRIM_IS_NEGATIVE; break;
				case 0x3B: instruction->opcode = PRIM_IS_DIVISIBLE_BY; break;
				case 0x3C: instruction->opcode = PRIM_IS_CLOSE_TO; break;
				case 0x3D: instruction->opcode = PRIM_DELAY_SECONDS; break;
				case 0x3E: instruction->opcode = PRIM_DELAY_MINUTES; break;
				case 0x3F: instruction->opcode = PRIM_MINUTES; break;
				case 0x40: instruction->opcode = PRIM_SONAR_DIST_CM; break;
				case 0x41: instruction->opcode = PRIM_MATRIX_8x8_DISPLAY; break;
				case 0x42: instruction->opcode = PRIM_MOD; break;
				case 0x43: instruction->opcode = PRIM_TONE; break;
				case 0x44: instruction->opcode = PRIM_NO_TONE; break;
				case 0x45: instruction->opcode = PRIM_GET_PIN_MODE; break;
				case 0x46: instruction->opcode = PRIM_SET_PIN_MODE; break;
				case 0x47: instruction->opcode = PRIM_ATAN2; break;
				case 0x48: instruction->opcode = PRIM_GET_SERVO_DEGREES; break;
				case 0x49: instruction->opcode = PRIM_LCD_INIT0; break;
				case 0x50: instruction->opcode = PRIM_LCD_INIT1; break;
				case 0x51: instruction->opcode = PRIM_LCD_PRINT_VALUE; break;
				case 0x52: instruction->opcode = PRIM_ARRAY_INIT; break;
				case 0x53: instruction->opcode = PRIM_ARRAY_GET; break;
				case 0x54: instruction->opcode = PRIM_ARRAY_SET; break;
				case 0x55: instruction->opcode = PRIM_ARRAY_CLEAR; break;
				case 0x56: instruction->opcode = PRIM_ARRAY_SUM; break;
				case 0x57: instruction->opcode = PRIM_ARRAY_AVG; break;
				case 0x58: instruction->opcode = PRIM_ARRAY_MAX; break;
				case 0x59: instruction->opcode = PRIM_ARRAY_MIN; break;
			}
			argument = 0; // INFO(Richo): Primitives don't have arguments (at least, not yet)
		}
		break;

		case 0xFC:
		case 0x0C:
			instruction->opcode = SCRIPT_CALL;
			break;

		case 0xFD:
			instruction->opcode = argument & 0x80 ? SCRIPT_RESUME : SCRIPT_START;
			argument = argument & 0x7F;
			break;
		case 0x0D:
			instruction->opcode = argument & 0x08 ? SCRIPT_RESUME : SCRIPT_START;
			argument = argument & 0x07;
			break;

		case 0xFE:
			instruction->opcode = argument & 0x80 ? SCRIPT_PAUSE : SCRIPT_STOP;
			argument = argument & 0x7F;
			break;
		case 0x0E:
			instruction->opcode = argument & 0x08 ? SCRIPT_PAUSE : SCRIPT_STOP;
			argument = argument & 0x07;
			break;
	}
	instruction->data = (int8)argument & ~BREAKPOINT_MASK;
}

void setBreakpoint(Instruction* instruction, bool value) 
{
	if (value) 
	{
		instruction->data |= BREAKPOINT_MASK;
	}
	else 
	{
		instruction->data &= ~BREAKPOINT_MASK;
	}
}

bool getBreakpoint(Instruction* instruction) 
{
	return instruction->data & BREAKPOINT_MASK;
}

int8 getArgument(Instruction* instruction) 
{
	/*
	NOTE(Richo): The argument is retrieved in two steps.
	1) First, we remove the breakpoint flag.
	2) Then we extend the sign (from 7 bit to 8 bit)
	*/
	uint8 argument = instruction->data & ~BREAKPOINT_MASK;
	uint8 mask = (argument << 1) & BREAKPOINT_MASK;
	return (int8)(argument | mask);
}
