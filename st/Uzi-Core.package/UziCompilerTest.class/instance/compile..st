private
compile: aString
	UziCompileStatsReportGenerator current 
		register: aString 
		test: thisContext receiver selector
		class: self class.
	^ Uzi compileString: aString