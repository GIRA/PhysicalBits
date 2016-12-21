accessing
at: key ifPresent: presentBlock ifAbsent: absentBlock
	^ super 
		at: key asString asSymbol 
		ifPresent: presentBlock 
		ifAbsent: absentBlock