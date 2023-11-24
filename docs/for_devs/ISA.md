# Instruction Set
*Disclaimer: This is a work in progress. The content of this page could change at any moment and may not reflect the actual implementation.*

\# | Name | Binary encoding | Stack<br />[before]<br />→<br />[after] | Description
---|------|-----------------|--------------------------|------------
0 | TURN_ON | `000xxxxx`<br />*(x = pin)* | no change | Writes a HIGH value to *pin*
1 | TURN_OFF | `001xxxxx`<br />*(x = pin)* | no change | Writes a LOW value to *pin*
2 | WRITE_PIN | `010xxxxx`<br />*(x = pin)* | value → | Writes *value* to *pin*
3 | READ_PIN | `011xxxxx`<br />*(x = pin)* | → value | Reads from *pin* and pushes the *value* to the stack
4 | READ_GLOBAL | *Short:*<br />`1000xxxx`<br /><br />*Long:*<br />`11111000 xxxxxxxx`<br /><br />*(x = index)* | → value | Reads the *value* of the global variable at *index* and pushes it on the stack
5 | WRITE_GLOBAL | *Short:*<br />`1001xxxx`<br /><br />*Long:*<br />`11111001 xxxxxxxx`<br /><br />*(x = index)* | value → | Writes the *value* on the top of the stack to the global variable at *index*
6 | SCRIPT_CALL | *Short:*<br />`1100xxxx`<br /><br />*Long:*<br />`11111100 xxxxxxxx`<br /><br />*(x = index)* | ret_val,<br />(fp << 16 \| pc)<br />→ | Calls the script at *index*
7 | SCRIPT_START | *Short:*<br />`11010xxx`<br /><br />*Long:*<br />`11111101 0xxxxxxx`<br /><br />*(x = index)* | no change | Starts the execution of the script at *index*
8 | SCRIPT_STOP | *Short:*<br />`11100xxx`<br /><br />*Long:*<br />`11111110 0xxxxxxx`<br /><br />*(x = index)* | no change | Stops the execution of the script at *index*
9 | SCRIPT_PAUSE | *Short:*<br />`11101xxx`<br /><br />*Long:*<br />`11111110 1xxxxxxx`<br /><br />*(x = index)* | no change | Pauses the execution of the script at *index*
10 | SCRIPT_RESUME | *Short:*<br />`11011xxx`<br /><br />*Long:*<br />`11111101 1xxxxxxx`<br /><br />*(x = index)* | no change | Resumes the execution of the script at *index*
11 | READ_LOCAL | `11111111 0xxxxxxx`<br />*(x = offset)* | → value | Reads the *value* of the local variable at *fp* + *offset* and pushes it on the stack
12 | WRITE_LOCAL | `11111111 1xxxxxxx`<br />*(x = offset)* | value → | Writes the *value* on the top of the stack to the local variable at *fp* + *offset*
13 | JMP | `11110000 xxxxxxxx`<br />*(x = offset)* | no change | Jumps to the instruction at *pc* + *offset*
14 | JZ | `11110001 xxxxxxxx`<br />*(x = offset)* | value → | Jumps to the instruction at *pc* + *offset* if *value* equals zero
15 | JNZ | `11110010 xxxxxxxx`<br />*(x = offset)* | value → | Jumps to the instruction at *pc* + *offset* if *value* doesn't equal zero
16 | JLTE | `11110101 xxxxxxxx`<br />*(x = offset)* | value1, value2 → | Jumps to the instruction at *pc* + *offset* if *value1* is less than or equals to *value2*
17 | PRIM_READ_PIN | `10100000` | pin → value | Reads from *pin* and pushes the *value* to the stack
18 | PRIM_WRITE_PIN | `10100001` | value, pin → | Writes *value* to *pin*
19 | PRIM_SERVO_WRITE | `10100100` | value, pin → | Writes *value* to a servo at *pin*
20 | PRIM_MULTIPLY | `10100101` | num1, num2 → result | Multiplies *num1* with *num2* and pushes the *result* to the stack
21 | PRIM_ADD | `10100110` | num1, num2 → result | Adds *num1* with *num2* and pushes the *result* to the stack
22 | PRIM_DIVIDE | `10100111` | num1, num2 → result | Divides *num1* with *num2* and pushes the *result* to the stack
23 | PRIM_SUBTRACT | `10101000` | num1, num2 → result | Subtracts *num1* with *num2* and pushes the *result* to the stack
24 | PRIM_EQ | `10101010` | val1, val2 → result | Pushes 1 if *val1* equals *val2*, pushes 0 otherwise
25 | PRIM_NEQ | `10101011` | val1, val2 → result | Pushes 1 if *val1* doesn't equal *val2*, pushes 0 otherwise
26 | PRIM_GT | `10101100` | val1, val2 → result | Pushes 1 if *val1* is greater than *val2*, pushes 0 otherwise
27 | PRIM_GTEQ | `10101101` | val1, val2 → result | Pushes 1 if *val1* is greater than or equals to *val2*, pushes 0 otherwise
28 | PRIM_LT | `10101110` | val1, val2 → result | Pushes 1 if *val1* is less than *val2*, pushes 0 otherwise
29 | PRIM_LTEQ | `10101111` | val1, val2 → result | Pushes 1 if *val1* is less than or equals to *val2*, pushes 0 otherwise
30 | PRIM_NEGATE | `10110000` | num → result | Multiplies *num* by -1 and pushes the *result* to the stack
31 | PRIM_SIN | `10110001` | num → result | Pushes the sine of *num* to the stack
32 | PRIM_COS | `10110010` | num → result | Pushes the cosine of *num* to the stack
33 | PRIM_TAN | `10110011` | num → result | Pushes the tangent of *num* to the stack
34 | PRIM_YIELD | `10110110` | no change | Yields control to the next available task
35 | PRIM_DELAY_MILLIS | `10110111` | time → | Suspends the execution of the current task for *time* ms
36 | PRIM_MILLIS | `10111000` | → ms | Pushes the number of *ms* since the board started
37 | PRIM_RET | `10111001` | ? → | Returns from the currently executing script leaving no return value at the top of the stack
38 | PRIM_POP | `10111010` | value → | Pops the *value* on the top of the stack (and throws it away)
39 | PRIM_RETV | `10111011` | ? → return | Returns from the currently executing script leaving the *return* value at the top of the stack
40 | PRIM_COROUTINE | `10111100` | → result | Pushes the id of the current task on the stack
41 | PRIM_LOGICAL_AND | `10111101` | val1, val2 → result | Pushes 1 if both *val1* and *val2* are true, pushes 0 otherwise
42 | PRIM_LOGICAL_OR | `10111110` | val1, val2 → result | Pushes 1 if either *val1* or *val2* are true, pushes 0 otherwise
43 | PRIM_BITWISE_AND | `10111111` | val1, val2 → result | Performs a bitwise AND of *val1* with *val2* and pushes the *result* to the stack (both operands are treated as unsigned 32-bit integers)
44 | PRIM_BITWISE_OR | `11111010 00000000` | val1, val2 → result | Performs a bitwise OR of *val1* with *val2* and pushes the *result* to the stack (both operands are treated as unsigned 32-bit integers)
45 | PRIM_SERIAL_WRITE | `11111010 00000001` | byte → | Writes a single *byte* to the serial port
46 | PRIM_ROUND | `11111010 00000010` | num → result | Rounds *num* and pushes the *result* to the stack
47 | PRIM_CEIL | `11111010 00000011` | num → result | Rounds *num* upward and pushes the *result* to the stack
48 | PRIM_FLOOR | `11111010 00000100` | num → result | Rounds *num* to the closest smaller integer and pushes the *result* to the stack
49 | PRIM_SQRT | `11111010 00000101` | num → result | Computes the square root of *num* and pushes the *result* to the stack
50 | PRIM_ABS | `11111010 00000110` | num → result | Computes the absolute value of *num* and pushes the *result* to the stack
51 | PRIM_LN | `11111010 00000111` | num → result | Computes the natural logarithm of *num* and pushes the *result* to the stack
52 | PRIM_LOG10 | `11111010 00001000` | num → result | Computes the common logarithm of *num* and pushes the *result* to the stack
53 | PRIM_EXP | `11111010 00001001` | num → result | Computes e (Euler's number, 2.7182818...) raised to the given power *num*
54 | PRIM_POW10 | `11111010 00001010` | num → result | Computes 10 to the power of *num*
55 | PRIM_ASIN | `11111010 00001011` | num → result | Pushes the arcsine of *num* to the stack
56 | PRIM_ACOS | `11111010 00001100` | num → result | Pushes the arccosine of *num* to the stack
57 | PRIM_ATAN | `11111010 00001101` | num → result | Pushes the arctangent of *num* to the stack
58 | PRIM_POWER | `11111010 00001110` | num1, num2 → result | Computes *num1* raised to the power of *num2* and pushes the *result* to the stack
59 | PRIM_REMAINDER | `11111010 00010001` | numer, denom → result | Computes the floating-point remainder of *numer* / *denom* (rounded towards zero) and pushes the *result* to the stack
60 | PRIM_RANDOM_INT | `11111010 00010011` | num1, num2 → result | Generates a random integer between *num1* and *num2* and pushes it to the stack
61 | PRIM_RANDOM | `11111010 00010100` | → result | Generates a random floating-point number between 0 and 1 and pushes it to the stack
62 | PRIM_SONAR_DIST_CM | `11111010 00100000` | trig, echo, max_dist → dist_cm | Sends a ping using an ultrasonic sensor on pins *trig* and *echo*, measures the echo time to calculate distance in *cm*, and pushes the resulting distance to the stack
63 | PRIM_MATRIX_8x8_DISPLAY | `11111010 00100001` | ? | Interfaces with a 8x8 matrix display. Unused.
64 | PRIM_TONE | `11111010 00100011` | pin, freq → | Generates a tone on *pin* of the given frequency *freq*
65 | PRIM_NO_TONE | `11111010 00100100` | pin → | Cancels the tone on *pin*
66 | PRIM_GET_PIN_MODE | `11111010 00100101` | pin → mode | Pushes the *mode* of *pin* on to the stack
67 | PRIM_SET_PIN_MODE | `11111010 00100110` | pin, mode → | Sets the *mode* of *pin*
68 | PRIM_ATAN2 | `11111010 00100111` | y, x → result | Computes the arc tangent of *y*/*x* and pushes the *result* to the stack
69 | PRIM_LCD_INIT0 | `11111010 00101001` | address, cols, rows → pointer | Creates an LCD object on the I2C *address* with the given number of columns and rows (*cols* and *rows*, respectively) and pushes a *pointer* to it to the stack
70 | PRIM_LCD_INIT1 | `11111010 00110000` | pointer → pointer | Initializes the LCD object on *pointer*, leaving it ready to be used (before executing this instruction one needs a *pointer* to an uninitialized LCD object)
71 | PRIM_LCD_PRINT_VALUE | `11111010 00110001` | pointer, value → | Prints the given numeric *value* to the LCD display at *pointer*
72 | PRIM_ARRAY_INIT | `11111010 00110010` | size → pointer | Creates an array of the given *size* and pushes a *pointer* to the first element to the stack
73 | PRIM_ARRAY_GET | `11111010 00110011` | pointer, index → value | Accesses the array on *pointer* and pushes the *value* at the given *index* to the stack
74 | PRIM_ARRAY_SET | `11111010 00110100` | pointer, index, value → | Accesses the array on *pointer*, setting the *value* at the given *index*
75 | PRIM_JMP | `11111010 01000000` | offset → | Jumps to the instruction at *pc* + *offset*
76 | PRIM_JZ | `11111010 01000001` | value, offset → | Jumps to the instruction at *pc* + *offset* if *value* equals zero
77 | PRIM_JNZ | `11111010 01000010` | value, offset → | Jumps to the instruction at *pc* + *offset* if *value* doesn't equal zero
78 | PRIM_JLTE | `11111010 01000011` | value1, value2, offset → | Jumps to the instruction at *pc* + *offset* if *value1* is less than or equals to *value2*
79 | PRIM_LCD_PRINT_STRING | `11111010 01000100` | pointer, value → | Prints the given string *value* to the LCD diplay at *pointer*
80 | PRIM_LCD_CLEAR | `11111010 01000101` | pointer → | Clears the screen of LCD display on *pointer*
81 | PRIM_LCD_SET_CURSOR | `11111010 01000110` | pointer, row, column → | Moves the cursor of the LCD display on *pointer* to the given *row* and *column*
82 | PRIM_STRING_LENGTH | `11111010 01000111` | pointer → length | Computes the *length* of the string at *pointer* and pushes it to the stack