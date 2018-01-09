accessing
defaultGlobals
	^ self class defaultGlobals collect: [:each | UziVariable value: each]