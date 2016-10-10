installing
runOnce: anUziProgram
	^ self send: #[9] , (UziEncoder new encode: anUziProgram)