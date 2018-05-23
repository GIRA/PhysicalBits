private
varNameFor: variable
	^ self localsInScope 
		detect: [:each | variable name = each name]
		ifFound: [:var | variableNames at: var]
		ifNone: [variable name].