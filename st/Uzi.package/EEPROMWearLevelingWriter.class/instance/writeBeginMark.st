initialize-release
writeBeginMark
	"Erase previous end mark and then write the begin mark"
	writer 
		nextPut: 0;
		nextPut: self beginMark 