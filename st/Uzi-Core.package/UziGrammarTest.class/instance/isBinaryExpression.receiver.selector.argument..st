utils
isBinaryExpression: expr receiver: rcvrBlock selector: selector argument: argBlock 
	expr size = 2 ifFalse: [^ false].
	(rcvrBlock value: expr first) ifFalse: [^ false].
	^ expr second size = 1 
		and: [expr second allSatisfy: [:msg |
			self isBinaryMessage: msg selector: selector argument: argBlock]]