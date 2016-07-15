initialize-release
writeBeginMark
	"Make sure the begin mark is not escaped by clearing the current position
	and then writing the begin mark"
	writer 
		nextPut: 0;
		nextPut: self beginMark 