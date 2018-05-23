accessing
localsInScope
	^ self variablesInScope copyWithoutAll: self path last globals