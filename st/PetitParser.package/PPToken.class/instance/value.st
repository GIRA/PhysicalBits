accessing-values
value
	self notify: 'Token>>#value is no longer supported. Instead use Token>>#inputValue or the more pragmatic #parsedValue.'.
	^ self inputValue