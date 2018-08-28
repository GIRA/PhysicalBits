visiting
visitCallNode: node
	self saveIntervalFor: node
		during: [
			(node selector allSatisfy: [:char | char isAlphaNumeric])
				ifTrue: [^ self writeRegularCall: node].
			node arguments size = 1
				ifTrue: [^ self writeUnaryCall: node].
			node arguments size = 2
				ifTrue: [^ self writeBinaryCall: node].
			
			self error: 'Invalid call']