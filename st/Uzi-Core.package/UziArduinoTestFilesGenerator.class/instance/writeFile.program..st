as yet unclassified
writeFile: fileName program: program
	self writeFile: fileName contents: (UziController new run: program)