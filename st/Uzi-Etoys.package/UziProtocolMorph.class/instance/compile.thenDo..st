running programs
compile: aString thenDo: aBlock
	[| encoded program |
	aBlock value: (program := UziCompiler new compile: aString).
	encoded := UziEncoder new encode: program.
	Transcript 
		cr; 
		show: 'Compilation successful!'; cr;
		show: 'Program size (bytes): '; show: encoded size; cr;
		show: encoded printString; cr]
		on: UziParseError , UziCompileError 
		do: [:err | Transcript cr; show: err asString; cr.
			UIManager default inform: 'Compilation error! See transcript for details']