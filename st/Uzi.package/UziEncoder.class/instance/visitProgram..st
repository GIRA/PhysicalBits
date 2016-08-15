encoding programs
visitProgram: anUziProgram
	self writeProgramScriptCount: anUziProgram;
		writeVariables: anUziProgram;
		visitCollection: anUziProgram scripts