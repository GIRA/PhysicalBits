accessing
allGlobals
	| default |
	default := self defaultGlobals.
	^ default, (self sortedGlobals copyWithoutAll: default)