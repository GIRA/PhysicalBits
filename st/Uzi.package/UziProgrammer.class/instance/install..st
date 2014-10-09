installing
install: anUziScript
	port nextPutAll: #[0] , (UziEncoder new encode: anUziScript)