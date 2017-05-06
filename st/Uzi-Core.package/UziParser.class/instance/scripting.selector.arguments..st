building messages
scripting: rcvr selector: selector arguments: args
	^  selector caseOf: {
		"Scripting"
		[#start] 
		-> [UziScriptStartNode script: rcvr].
		[#stop] 
		-> [UziScriptStopNode script: rcvr].
		[#value]
		-> [UziScriptCallNode script: rcvr]
	} 
	otherwise: [rcvr isScriptReference 
		ifTrue: [| argNames |
			argNames := selector subStrings: ':'.
			UziScriptCallNode 
				script: rcvr 
				arguments: (argNames 
					with: args 
					collect: [:name :arg | name asSymbol -> arg])]
		ifFalse: [nil]].