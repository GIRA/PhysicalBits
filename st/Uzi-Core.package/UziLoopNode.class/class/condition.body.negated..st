instance creation
condition: conditionNode body: bodyNode negated: negatedBool
	^ self basicNew
		initializeWithCondition: conditionNode
		body: bodyNode
		negated: negatedBool