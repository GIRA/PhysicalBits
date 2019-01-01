initialize-release
initialize
	| acc |
	super initialize.
	acc := 0.
	scripts := OrderedCollection new.
	program scripts do: [:script |
		scripts add: (UziDebuggerScript 
			on: script 
			start: acc).
		acc := acc + script instructions size]