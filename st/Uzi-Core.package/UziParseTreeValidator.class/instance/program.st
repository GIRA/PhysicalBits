accessing
program
	^ self path detect: [:node | node isProgram] ifNone: [nil]