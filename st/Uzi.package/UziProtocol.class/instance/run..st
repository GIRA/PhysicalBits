installing
run: anUziProgram
	currentProgram := anUziProgram.
	globals := Dictionary new.
	^ self send: #[0] , (UziEncoder new encode: anUziProgram)