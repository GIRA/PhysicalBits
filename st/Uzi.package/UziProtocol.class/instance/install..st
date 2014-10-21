installing
install: anUziScript
	self send: #[0] , (UziEncoder new encode: anUziScript)