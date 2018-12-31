accessing
instructions
	^ Array streamContents: [:stream |
		scripts do: [:script | stream nextPutAll: script instructions]]