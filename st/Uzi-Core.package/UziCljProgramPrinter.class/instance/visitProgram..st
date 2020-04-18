encoding programs
visitProgram: anUziProgram
	program := anUziProgram.
	self writeLine: '(emit/program'.
	self indent: 3 
		during: [
			self writeGlobals: anUziProgram globals.
			self writeScripts: anUziProgram scripts.
			self write: ')']