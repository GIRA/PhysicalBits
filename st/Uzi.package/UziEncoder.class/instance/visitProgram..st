visiting
visitProgram: anUziProgram
	self writeProgramScriptCount: anUziProgram;
		writeProgramPins: anUziProgram inputs;
		writeProgramPins: anUziProgram outputs;
		visitCollection: anUziProgram scripts