utils
isKeywordExpression: expr receiver: rcvrBlock selectors: selectors arguments: argsBlock
	expr size = 2 ifFalse: [^ false].
	(expr first size = 2 and: [expr first second isEmpty])
		ifFalse: [^ false].
	(rcvrBlock value: expr first first) ifFalse: [^ false].
	^ self isKeywordMessage: expr second
		selectors: selectors
		arguments: argsBlock