accessing
script
	^ self path detect: [:node | node isScript] ifNone: [nil]