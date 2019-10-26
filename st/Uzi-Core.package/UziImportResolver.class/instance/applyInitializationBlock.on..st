private
applyInitializationBlock: initBlock on: ast
	| globals scripts |
	globals := Dictionary new.
	initBlock statements do: [:stmt |
		(stmt isAssignment 
			and: [stmt right isNumberLiteral
				or: [stmt right isPinLiteral]]) 
			ifTrue: [	globals at: stmt left name put: stmt right]].
			
	scripts := Dictionary new.
	initBlock statements do: [:stmt |
		stmt isScriptControl 
			ifTrue: [| state |
				state := nil.
				(stmt isScriptStart or: [stmt isScriptResume])
					ifTrue: [state := #running].
				(stmt isScriptStop or: [stmt isScriptPause])
					ifTrue: [state := #stopped].
				stmt scriptNames do: [:scriptName |
					scripts at: scriptName put: state]]].
			
	^ (UziProgramNode
		imports: ast imports
		globals: (ast globals collect: [:g |
			globals at: g name
				ifPresent: [:value | g withValue: value]
				ifAbsent: [g]])
		scripts: (ast scripts collect: [:s |
			scripts at: s name
				ifPresent: [:state | s state = #once 
					ifTrue: [s]
					ifFalse: [s withState: state]]
				ifAbsent: [s]])
		primitives: ast primitives)
		token: ast token