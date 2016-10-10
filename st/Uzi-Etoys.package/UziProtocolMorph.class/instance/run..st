running programs
run: aString
	protocol run: (UziCompiler new compile: aString).
	self rebuild