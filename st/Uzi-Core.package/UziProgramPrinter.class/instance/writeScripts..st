encoding programs
writeScripts: scripts
	scripts 
		do: [:s | self visit: s]
		separatedBy: [self writeLine: ';']