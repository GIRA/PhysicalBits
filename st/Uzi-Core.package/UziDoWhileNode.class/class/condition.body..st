instance creation
condition: conditionExpression body: bodyBlock
	^ self pre: bodyBlock
		condition: conditionExpression
		negated: false
		post: UziBlockNode empty 