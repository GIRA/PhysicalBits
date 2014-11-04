installing
install: anUziProgram
	self send: #[0] , (UziEncoder new encode: anUziProgram)