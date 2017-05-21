accessing
currentScript
	^ pc ifNotNil: [self scriptForPC: pc]