utils
isKeywordMessage: msg selectors: selectors arguments: argsBlock
	msg size = 2 ifFalse: [^ false].
	(msg first size = selectors size
		and: [msg second size = selectors size])
			ifFalse: [^ false].
	msg first with: selectors do: [:token :sel |
		token inputValue = sel ifFalse: [^ false]].
	^ argsBlock value: msg second