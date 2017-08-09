as yet unclassified
condition: conditionExpression body: bodyBlock
	^ self pre: bodyBlock
		condition: conditionExpression
		negated: true
		post: UziBlockNode empty 