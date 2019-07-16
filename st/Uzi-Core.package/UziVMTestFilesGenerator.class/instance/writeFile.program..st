utils
writeFile: fileName program: program
	self writeFile: fileName contents: (UziController new run: program)