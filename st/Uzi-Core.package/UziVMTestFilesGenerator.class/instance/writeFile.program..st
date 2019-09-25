utils
writeFile: fileName program: program
	UziVMTestFilesGeneratorResource current 
		register: {
			#testName -> fileName.
			#instructionCount -> program instructions size.
			#globalCount -> program globals size.
			#encodedSize -> (Uzi encode: program) size.
			#compileTime -> (program node
				ifNotNil: [:ast || src |
					src := UziParseNodePrinter new print: ast.
					[Uzi compileString: src] timeToRun])
		}.
	self writeFile: fileName contents: (UziController new run: program)