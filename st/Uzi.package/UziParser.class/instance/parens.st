grammar
parens
	^ super parens
		map: [:openToken :expressionNode :closeToken | expressionNode]