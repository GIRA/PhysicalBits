accessing
at: key ifPresent: presentBlock ifAbsent: absentBlock
	^ children
		at: key
		ifPresent: presentBlock
		ifAbsent: absentBlock