instance creation
condition: conditionNode
	^ self condition: conditionNode
		body: UziBlockNode empty
		negated: false
		evaluateBodyFirst: false