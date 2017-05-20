encoding programs
visitProgram: anUziProgram
	program := anUziProgram.
	self writeProgramScriptCount: anUziProgram;
		writeGlobals: anUziProgram;
		visitCollection: anUziProgram scripts