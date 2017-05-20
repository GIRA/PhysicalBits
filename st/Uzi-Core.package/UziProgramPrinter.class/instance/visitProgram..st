encoding programs
visitProgram: anUziProgram
	program := anUziProgram.
	self writeLine: 'Uzi program: [:p | p'.
	self indent: 1 
		during: [
			self writeGlobals: anUziProgram variables.
			self writeScripts: anUziProgram scripts.
			self write: '].']