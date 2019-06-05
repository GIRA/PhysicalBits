compiling
compileString: aString
	| program encoded |
	[program := Uzi compileString: aString.
	encoded :=  Uzi encode: program.
	Uzi out cr; 
		log: 'Compilation successful!';
		log: 'Program size (bytes): {1}' format: { encoded size };
		log: encoded printString;
		flush.
	^ program]
			on: UziParseError , UziCompileError 
			do: [:err | Uzi out cr; log: err asString; flush.
				err signal]