installing
run: anUziProgram
	currentProgram := anUziProgram.
	^ self send: #[0] , (UziEncoder new encode: anUziProgram)