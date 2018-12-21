accessing
temporaries
	| index |
	index := 2 + fp + self script variables size.
	^ stack copyFrom: index + 1 to: stack size.