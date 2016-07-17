installing
run: anUziProgram
	^ self send: #[0] , (UziEncoder new encode: anUziProgram)