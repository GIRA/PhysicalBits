utils
writeFile: fileName program: programOrString
	| program |
	UziCompileStatsReportGenerator current 
		register: programOrString test: fileName class: self class.
		
	program := programOrString isString
		ifTrue: [Uzi compileString: programOrString]
		ifFalse: [programOrString].
	
	programOrString isString
		ifTrue: [	UziCompileStatsReportGenerator current 
			writeSrc: programOrString
			encoded: (Uzi encode: program)
			testname: fileName].
	
	self writeFile: fileName capitalized contents: (UziController new run: program)