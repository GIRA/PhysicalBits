visiting
visitPrimitiveCall: node
	node selector = #not
		ifTrue: [self write: '!('; visit: node arguments first; write: ')']
		ifFalse: [(node selector allSatisfy: [:char | char isAlphaNumeric])
			ifTrue: [self writeRegularCall: node]
			ifFalse: [self writeBinaryCall: node]]