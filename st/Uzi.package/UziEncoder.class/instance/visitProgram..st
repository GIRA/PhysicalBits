encoding programs
visitProgram: anUziProgram
	program := anUziProgram.
	self writeProgramScriptCount: anUziProgram;
		writeVariables: anUziProgram;
		visitCollection: anUziProgram scripts