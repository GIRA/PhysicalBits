# Instruction Set
*Disclaimer: This is a work in progress. The content of this page could change at any moment and may not reflect the actual implementation.*

\# | Name | Binary encoding | Stack [before] → [after] | Description
---|------|-----------------|--------------------------|------------
0 | TURN_ON | `000xxxxx` *(x = pin)* | no change | Write a HIGH value to *pin*
1 | TURN_OFF | `001xxxxx` *(x = pin)* | no change | Write a LOW value to *pin*
2 | WRITE_PIN | `010xxxxx` *(x = pin)* | value → | Write *value* to *pin*
3 | READ_PIN | `011xxxxx` *(x = pin)* | → value | Read *value* from *pin*
4 | READ_GLOBAL | `1000xxxx` *(x = index)* | → value | Read the *value* of the global variable at *index* and push it on the stack
5 | WRITE_GLOBAL | `1001xxxx` *(x = index)* | value → | Write the *value* on the top of the stack to the global variable at *index*
6 | SCRIPT_CALL | `1100xxxx` *(x = index)* | ret_val, (fp << 16 \| pc) → | Call the script at *index*
7 | SCRIPT_START | `11010xxx` *(x = index)* | no change | Start script at *index*
8 | SCRIPT_RESUME | `11011xxx` *(x = index)* | no change | Resume script at *index*
9 | SCRIPT_STOP | `11100xxx` *(x = index)* | no change | Stop script at *index*
10 | SCRIPT_PAUSE | `11101xxx` *(x = index)* | no change | Pause script at *index*
11 | JMP | `11110000 xxxxxxxx` *(x = instr)* | no change | Jump to the instruction at *pc* + *instr*
12 | JZ | `11110001 xxxxxxxx` *(x = instr)* | value → | Jump to the instruction at *pc* + *instr* if *value* equals zero
13 | JNZ | `11110010 xxxxxxxx` *(x = instr)* | value → | Jump to the instruction at *pc* + *instr* if *value* doesn't equal zero
14 | JNE | `11110011 xxxxxxxx` *(x = instr)* | value1, value2 → | Jump to the instruction at *pc* + *instr* if *value1* doesn't equal *value2*
15 | JLT | `11110100 xxxxxxxx` *(x = instr)* | value1, value2 → | Jump to the instruction at *pc* + *instr* if *value1* is less than *value2*
16 | JLTE | `11110101 xxxxxxxx` *(x = instr)* | value1, value2 → | Jump to the instruction at *pc* + *instr* if *value1* is less than or equals to *value2*
17 | JGT | `11110110 xxxxxxxx` *(x = instr)* | value1, value2 → | Jump to the instruction at *pc* + *instr* if *value1* is greater than *value2*
18 | JGTE | `11110111 xxxxxxxx` *(x = instr)* | value1, value2 → | Jump to the instruction at *pc* + *instr* if *value1* is greater than or equals *value2*
19 | JGTE | `11110111 xxxxxxxx` *(x = instr)* | value1, value2 → | Jump to the instruction at *pc* + *instr* if *value1* is greater than or equals *value2*
20 | READ_GLOBAL_EXT | `11111000 xxxxxxxx` *(x = index)* | ? | Read the *value* of the global variable at *index* and push it on the stack
21 | WRITE_GLOBAL_EXT | `11111001 xxxxxxxx` *(x = index)* | ? | Write the *value* on the top of the stack to the global variable at *index*
22 | SCRIPT_CALL_EXT | `11111100 xxxxxxxx` *(x = index)* | ? | Call the script at *index*
23 | SCRIPT_START_EXT | `11111101 0xxxxxxx` *(x = index)* | ? | Start script at *index*
24 | SCRIPT_RESUME_EXT | `11111101 1xxxxxxx` *(x = index)* | ? | Resume script at *index*
25 | SCRIPT_STOP_EXT | `11111110 0xxxxxxx` *(x = index)* | ? | Stop script at *index*
26 | SCRIPT_PAUSE_EXT | `11111110 1xxxxxxx` *(x = index)* | ? | Pause script at *index*
27 | READ_LOCAL | `11111111 0xxxxxxx` *(x = offset)* | → value | Read the *value* of the local variable at *fp* + *offset* and push it on the stack
28 | WRITE_LOCAL | `11111111 1xxxxxxx` *(x = offset)* | value → | Write the *value* on the top of the stack to the local variable at *fp* + *offset*
29 | PRIM_READ_PIN | `10100000` | pin → value | Read *value* from *pin*
30 | PRIM_WRITE_PIN | `10100001` | value, pin → | Write *value* to *pin*
31 | PRIM_TOGGLE_PIN | `10100010` | pin → | Toggle *pin*
32 | PRIM_SERVO_DEGREES | `10100011` | degrees, pin → | Set *degrees* to servo at *pin*
33 | PRIM_SERVO_WRITE | `10100100` | value, pin → | Write *value* to servo at *pin*
34 | PRIM_MULTIPLY | `10100101` | val1, val2 → result | Multiply *val1* with *val2* and push the *result* to the stack
35 | PRIM_ADD | `10100110` | val1, val2 → result | Add *val1* with *val2* and push the *result* to the stack
36 | PRIM_DIVIDE | `10100111` | val1, val2 → result | Divide *val1* with *val2* and push the *result* to the stack
37 | PRIM_SUBTRACT | `10101000` | val1, val2 → result | Subtract *val1* with *val2* and push the *result* to the stack
38 | PRIM_SECONDS | `10101001` | → result | Push the number of seconds since the board started
39 | PRIM_EQ | `10101010` | val1, val2 → result | Push 1 if *val1* equals *val2*, 0 otherwise
40 | PRIM_NEQ | `10101011` | val1, val2 → result | Push 1 if *val1* doesn't equal *val2*, 0 otherwise
41 | PRIM_GT | `10101100` | val1, val2 → result | Push 1 if *val1* is greater than *val2*, 0 otherwise
42 | PRIM_GTEQ | `10101101` | val1, val2 → result | Push 1 if *val1* is greater than or equals to *val2*, 0 otherwise
43 | PRIM_LT | `10101110` | val1, val2 → result | Push 1 if *val1* is less than *val2*, 0 otherwise
44 | PRIM_LTEQ | `10101111` | val1, val2 → result | Push 1 if *val1* is less than or equals to *val2*, 0 otherwise
45 | PRIM_NEGATE | `10110000` | value → result | Multiply *value* by -1 and push the *result* to the stack
46 | PRIM_SIN | `10110001` | value → result | Push the sine of *value* to the stack
47 | PRIM_COS | `10110010` | value → result | Push the cosine of *value* to the stack
48 | PRIM_TAN | `10110011` | value → result | Push tangent of *value* to the stack
49 | PRIM_TURN_ON | `10110100` | pin → | Write a HIGH *value* to *pin*
50 | PRIM_TURN_OFF | `10110101` | pin → | Write a LOW *value* to *pin*
51 | PRIM_YIELD | `10110110` | no change | yield
52 | PRIM_YIELD_TIME | `10110111` | ms → | Pause the current task for the given *ms*
53 | PRIM_MILLIS | `10111000` | → ms | Push the number of *ms* since the board started
54 | PRIM_RET | `10111001` | ? | Return from the currently executing script
55 | PRIM_POP | `10111010` | value → | Pop the *value* on the top of the stack (and throw it away)
56 | PRIM_RETV | `10111011` | ? | Return from the currently executing script leaving the value at the top of the stack as the return value
57 | PRIM_READ_GLOBAL | `10111100` | index → value | Read the *value* of the global variable at *index* and push it on the stack
58 | PRIM_WRITE_GLOBAL | `10111101` | value, index → | Write *value* to the global variable at *index*
59 | PRIM_READ_LOCAL | `10111110` | offset → value | Read the *value* of the local variable at *fp* + *offset* and push it on the stack
60 | PRIM_WRITE_LOCAL | `10111111` | value, offset → | Write *value* to the local variable at *fp* + *offset*
61 | PRIM_COROUTINE | `11111010 00000000` | → result | Push the id of the current task on the stack
62 | PRIM_LOGICAL_AND | `11111010 00000001` | val1, val2 → result | Push 1 if both *val1* and *val2* are true, 0 otherwise
63 | PRIM_LOGICAL_OR | `11111010 00000010` | val1, val2 → result | Push 1 if either *val1* or *val2* are true, 0 otherwise
64 | PRIM_BITWISE_AND | `11111010 00000011` | val1, val2 → result | Perform a bitwise AND of *val1* with *val2* and push the *result* to the stack (both operands are treated as unsigned 32-bit integers)
65 | PRIM_BITWISE_OR | `11111010 00000100` | val1, val2 → result | Perform a bitwise OR of *val1* with *val2* and push the *result* to the stack (both operands are treated as unsigned 32-bit integers)
