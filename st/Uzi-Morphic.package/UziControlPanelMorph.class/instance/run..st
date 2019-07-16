running programs
run: aString
	| program |
	[program := controller compileString: aString.
	controller run: program]
		on: UziParseError , UziCompileError 
		do: [:err | UIManager default 
			inform: 'Compilation error! See transcript for details']