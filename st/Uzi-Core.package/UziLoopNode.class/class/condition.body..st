instance creation
condition: conditionNode body: bodyNode
	^ self basicNew
		initializeWithCondition: conditionNode
		body: bodyNode
	