encoding scripts
visitScript: anUziScript 
	script := anUziScript.
	self writeLine: '(emit/script'.
	self indent: 4 during: [
		self indent; writeLine: ':name "{1}"' format: { anUziScript name asString };
			indent; writeLine: ':running? {1}' format: { anUziScript isTicking };
			indent; writeLine: ':delay {1}' format: { anUziScript delay value }.
		self indent; write: ':arguments ['.
		self indent: 3
			during: [anUziScript arguments
					do: [:var |
						self write: '(emit/variable "{1}" {2})'
							format: {var name. var value = Float infinity ifTrue: ['##Inf'] ifFalse: [var value]}]
					separatedBy: [self writeLine: ''; indent].
				self writeLine: ']'].

		self indent; write: ':locals ['.
		self indent: 2
			during: [anUziScript locals
					do: [:var |
						self write: '(emit/variable "{1}" {2})'
							format: {var name. var value = Float infinity ifTrue: ['##Inf'] ifFalse: [var value]}]
					separatedBy: [self writeLine: ''; indent. stream space].
				self writeLine: ']'].
	
		self indent;write: ':instructions ['.
		self indent: 4
			during: [| index |
				index := 0.
				anUziScript instructions
					do: [:instr |
						index > 0 ifTrue: [self indent].
						self visit: instr.
						index := index + 1]
					separatedBy: [self writeLine: ''].
				self write: ']'.
				self write: ')']]