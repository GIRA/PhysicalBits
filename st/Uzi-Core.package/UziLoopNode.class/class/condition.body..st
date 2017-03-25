instance creation
condition: conditionNode body: bodyNode
	^ self condition: conditionNode
		body: bodyNode
		negated: false
		evaluateBodyFirst: false