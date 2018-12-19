running programs
run: aString
	| program |
	[program := protocol compileString: aString.
	protocol run: program]
		on: UziParseError , UziCompileError 
		do: [:err | UIManager default 
			inform: 'Compilation error! See transcript for details']