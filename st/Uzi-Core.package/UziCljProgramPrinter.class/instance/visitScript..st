encoding scripts
visitScript: anUziScript 
	script := anUziScript.
	self writeLine: '(emit/script'.
	self indent; indent; indent; writeLine: ':name "{1}"' format: { anUziScript name asString };
		indent; indent; indent; writeLine: ':running? {1}' format: { anUziScript isTicking };
		indent; indent; indent; writeLine: ':delay {1}' format: { anUziScript delay value }.
	self indent; indent; indent; write: ':arguments ['.
	self indent: 4
		during: [| index |
			index := 0.
			anUziScript arguments
				do: [:var |
					index > 0 ifTrue: [self indent; indent].
					self write: '(emit/variable "{1}" {2})'
						format: {var name. var value = Float infinity ifTrue: ['##Inf'] ifFalse: [var value]}.
					index := index + 1]
				separatedBy: [self writeLine: ''].
			self writeLine: ']'].

	self indent; indent; indent; write: ':locals ['.
	self indent: 4
		during: [| index |
			index := 0.
			anUziScript locals
				do: [:var |
					index > 0 ifTrue: [self indent; indent].
					self write: '(emit/variable "{1}" {2})'
						format: {var name. var value = Float infinity ifTrue: ['##Inf'] ifFalse: [var value]}.
					index := index + 1]
				separatedBy: [self writeLine: ''].
			self writeLine: ']'].
	
	self indent; indent; indent; write: ':instructions ['.
	self indent: 5
		during: [| index |
			index := 0.
			anUziScript instructions
				do: [:instr |
					index > 0 ifTrue: [self indent; indent].
					self visit: instr.
					index := index + 1]
				separatedBy: [self writeLine: ''].
			self write: ']'.
			self write: ')']