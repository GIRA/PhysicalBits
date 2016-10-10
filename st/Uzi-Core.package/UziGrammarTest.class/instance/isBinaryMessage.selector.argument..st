utils
isBinaryMessage: msg selector: selector argument: argBlock
	msg size = 2 ifFalse: [^ false].
	(msg first size = 1 and: [msg first first inputValue = selector])
		ifFalse: [^ false].
	^ msg second size = 1 and: [msg second allSatisfy: argBlock]