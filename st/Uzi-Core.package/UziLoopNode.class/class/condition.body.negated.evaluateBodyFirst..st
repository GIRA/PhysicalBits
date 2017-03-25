instance creation
condition: conditionNode body: bodyNode negated: negatedBool evaluateBodyFirst: bodyFirstBool
	^ self basicNew
		initializeWithCondition: conditionNode
		body: bodyNode
		negated: negatedBool
		evaluateBodyFirst: bodyFirstBool