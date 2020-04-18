utils
writeFile: fileName program: programOrString
	| program |
	UziCompileStatsReportGenerator current 
		register: programOrString test: fileName class: self class.
		
	program := programOrString isString
		ifTrue: [Uzi compileString: programOrString]
		ifFalse: [programOrString].
	
	UziCompileStatsReportGenerator current 
		writeCLJ: program
		encoded: (Uzi encode: program)
		testname: fileName.
	
	self writeFile: fileName capitalized contents: (UziController new run: program)