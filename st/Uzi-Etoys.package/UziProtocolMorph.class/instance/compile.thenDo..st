running programs
compile: aString thenDo: aBlock
	[| encoded program |
	aBlock value: (program := Uzi compileString: aString).
	encoded := UziEncoder new encode: program.
	UziTranscript cr; 
		log: 'Compilation successful!';
		log: 'Program size (bytes): {1}' format: { encoded size };
		log: encoded printString]
			on: UziParseError , UziCompileError 
			do: [:err | UziTranscript cr; log: err asString.
				UIManager default inform: 'Compilation error! See transcript for details']