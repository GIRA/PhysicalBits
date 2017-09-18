# Instruction Set
*Disclaimer: This is a work in progress. The content of this page could change at any moment.*

\# | Name | Binary encoding | Stack [before] → [after] | Description
---|------|-----------------|---------------------------|------------
0 | TURN_ON | `000xxxxx` *(x = pin)* | no change | Write a HIGH value to pin
1 | TURN_OFF | `001xxxxx` *(x = pin)* | no change | Write a LOW value to pin
2 | WRITE_PIN | `010xxxxx` *(x = pin)* | value → | Write value to pin
3 | READ_PIN | `011xxxxx` *(x = pin)* | → value | Read value from pin
4 | READ_GLOBAL | `1000xxxx` *(x = var_index)* | → value | Read the value of the global variable at var_index and push it on the stack
5 | WRITE_GLOBAL | `1001xxxx` *(x = var_index)* | value → | Write the value on the top of the stack to the global variable at var_index
6 | PRIM_CALL_0 | `1010xxxx` *(x = prim_index)* | ? | Call the primitive at prim_index
7 | PRIM_CALL_1 | `1011xxxx` *(x = prim_index + 16)* | ? | Call the primitive at prim_index
8 | SCRIPT_CALL | `1100xxxx` *(x = script_index)* | ret_val, (fp << 16 \| pc) → | Call the script at script_index
9 | SCRIPT_START | `11010xxx` *(x = script_index)* | no change | Start script at script_index
10 | SCRIPT_RESUME | `11011xxx` *(x = script_index)* | no change | Resume script at script_index
11 | SCRIPT_STOP | `11100xxx` *(x = script_index)* | no change | Stop script at script_index
12 | SCRIPT_PAUSE | `11101xxx` *(x = script_index)* | no change | Pause script at script_index
13 | JMP | `11110000 xxxxxxxx` *(x = instr_to_jump)* | no change | Jump to the instruction at pc + instr_to_jump
14 | JZ | `11110001 xxxxxxxx` *(x = instr_to_jump)* | value → | Jump to the instruction at pc + instr_to_jump if value equals zero
15 | JNZ | `11110010 xxxxxxxx` *(x = instr_to_jump)* | value → | Jump to the instruction at pc + instr_to_jump if value doesn't equal zero
16 | JNE | `11110011 xxxxxxxx` *(x = instr_to_jump)* | value1, value2 → | Jump to the instruction at pc + instr_to_jump if value1 doesn't equal value2
17 | JLT | `11110100 xxxxxxxx` *(x = instr_to_jump)* | value1, value2 → | Jump to the instruction at pc + instr_to_jump if value1 is less than value2
18 | JLTE | `11110101 xxxxxxxx` *(x = instr_to_jump)* | value1, value2 → | Jump to the instruction at pc + instr_to_jump if value1 is less than or equals to value2
19 | JGT | `11110110 xxxxxxxx` *(x = instr_to_jump)* | value1, value2 → | Jump to the instruction at pc + instr_to_jump if value1 is greater than value2
20 | JGTE | `11110111 xxxxxxxx` *(x = instr_to_jump)* | value1, value2 → | Jump to the instruction at pc + instr_to_jump if value1 is greater than or equals value2
20 | JGTE | `11110111 xxxxxxxx` *(x = instr_to_jump)* | value1, value2 → | Jump to the instruction at pc + instr_to_jump if value1 is greater than or equals value2
21 | READ_GLOBAL_EXT | `11111000 xxxxxxxx` *(x = var_index)* | ? | Read the value of the global variable at var_index and push it on the stack
22 | WRITE_GLOBAL_EXT | `11111001 xxxxxxxx` *(x = var_index)* | ? | Write the value on the top of the stack to the global variable at var_index
23 | PRIM_CALL_2 | `11111010 xxxxxxxx` *(x = prim_index + 32)* | ? | Call the primitive at prim_index
24 | PRIM_CALL_3 | `11111011 xxxxxxxx` *(x = prim_index + 288)* | ? | Call the primitive at prim_index
25 | SCRIPT_CALL_EXT | `11111100 xxxxxxxx` *(x = script_index)* | ? | Call the script at script_index
26 | SCRIPT_START_EXT | `11111101 0xxxxxxx` *(x = script_index)* | ? | Start script at script_index
27 | SCRIPT_RESUME_EXT | `11111101 1xxxxxxx` *(x = script_index)* | ? | Resume script at script_index
28 | SCRIPT_STOP_EXT | `11111110 0xxxxxxx` *(x = script_index)* | ? | Stop script at script_index
29 | SCRIPT_PAUSE_EXT | `11111110 1xxxxxxx` *(x = script_index)* | ? | Pause script at script_index
30 | READ_LOCAL | `11111111 0xxxxxxx` *(x = stack_index)* | → value | Read the value of the local variable at stack_index offset from fp and push it on the stack
31 | WRITE_LOCAL | `11111111 1xxxxxxx` *(x = stack_index)* | value → | Write the value on the top of the stack to the local variable at stack_index offset from fp
