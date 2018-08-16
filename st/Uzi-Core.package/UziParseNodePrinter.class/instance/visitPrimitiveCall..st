visiting
visitPrimitiveCall: node
	(node selector allSatisfy: [:char | char isAlphaNumeric])
		ifTrue: [self writeRegularCall: node]
		ifFalse: [self writeBinaryCall: node]
	