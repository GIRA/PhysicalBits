as yet unclassified
updateLabelArgument: labels	
	argument isString 
		ifTrue: [argument := labels 
			at: argument
			ifAbsent: [Error signal: 'Label not found']]