instance creation
condition: conditionExpression body: bodyBlock
	^ self pre: UziBlockNode empty 
		condition: conditionExpression
		negated: false
		post: bodyBlock