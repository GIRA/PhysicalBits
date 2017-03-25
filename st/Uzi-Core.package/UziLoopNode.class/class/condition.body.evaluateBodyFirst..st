instance creation
condition: conditionNode body: bodyNode evaluateBodyFirst: bodyFirstBool
	^ self condition: conditionNode
		body: bodyNode
		negated: false
		evaluateBodyFirst: bodyFirstBool