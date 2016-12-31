instance creation
condition: conditionNode true: trueNode false: falseNode
	^ self basicNew
		initializeWithCondition: conditionNode
		trueBranch: trueNode
		falseBranch: falseNode