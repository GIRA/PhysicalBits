utils
isUnaryMessage: msg value: val
	^ msg size = 2
		and: [msg first size = 1
		and: [msg first first inputValue = val
		and: [msg second isEmpty]]]