label update
updateLabelArgument: labels	
	argument isString 
		ifTrue: [argument := labels 
			at: argument
			ifAbsent: [UziLabelNotFoundError signal: argument]]