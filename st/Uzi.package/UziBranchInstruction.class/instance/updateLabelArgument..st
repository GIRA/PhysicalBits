as yet unclassified
updateLabelArgument: labels	
	argument isString 
		ifTrue: [argument := labels 
			at: argument
			ifAbsent: [UziLabelNotFoundError signal: argument]]