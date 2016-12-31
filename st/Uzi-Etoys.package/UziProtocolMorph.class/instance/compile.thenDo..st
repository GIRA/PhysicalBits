running programs
compile: aString thenDo: aBlock
	[aBlock value: (UziCompiler new compile: aString).
	Transcript cr; show: 'Compilation successful!'; cr]
		on: Error
		do: [:err | Transcript cr; show: err asString; cr.
			UIManager default inform: 'Compilation error! See transcript for details']