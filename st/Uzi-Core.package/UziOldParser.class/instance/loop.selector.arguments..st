building messages
loop: rcvr selector: selector arguments: args
	"The receiver and first arg (if available) should always be blocks"
	(rcvr isBlock not 
	or: [args notEmpty and: [args first isBlock not]])
		ifTrue: [^ nil].

	^  selector caseOf: {
		"Loops"
		[#whileTrue:]
		-> [UziLoopNode 
				pre: rcvr copyWithoutLastStatement 
				condition: rcvr lastStatement
				negated: false
				post: args first].
		[#whileFalse:]
		-> [UziLoopNode 
				pre: rcvr copyWithoutLastStatement
				condition: rcvr lastStatement 
				negated: true 
				post: args first].
		[#whileTrue]
		-> [UziLoopNode 
				pre: rcvr copyWithoutLastStatement
				condition: rcvr lastStatement
				negated: false
				post: UziBlockNode empty].
		[#whileFalse]
		-> [UziLoopNode 
				pre: rcvr copyWithoutLastStatement
				condition: rcvr lastStatement
				negated: true
				post: UziBlockNode empty].
		[#doWhileTrue:]
		-> [UziLoopNode 
				pre: (rcvr concat: args first copyWithoutLastStatement)
				condition: args first lastStatement
				negated: false
				post: UziBlockNode empty].	
		[#doWhileFalse:]
		-> [UziLoopNode 
				pre: (rcvr concat: args first copyWithoutLastStatement)
				condition: args first lastStatement
				negated: true
				post: UziBlockNode empty].
		[#untilTrue:]
		-> [UziLoopNode 
				pre: rcvr copyWithoutLastStatement 
				condition: rcvr lastStatement
				negated: true
				post: args first].
		[#untilFalse:]
		-> [UziLoopNode 
				pre: rcvr copyWithoutLastStatement
				condition: rcvr lastStatement 
				negated: false 
				post: args first].
		[#untilTrue]
		-> [UziLoopNode 
				pre: rcvr copyWithoutLastStatement
				condition: rcvr lastStatement
				negated: true
				post: UziBlockNode empty].
		[#untilFalse]
		-> [UziLoopNode 
				pre: rcvr copyWithoutLastStatement
				condition: rcvr lastStatement
				negated: false
				post: UziBlockNode empty].
		[#doUntilTrue:]
		-> [UziLoopNode 
				pre: (rcvr concat: args first copyWithoutLastStatement)
				condition: args first lastStatement
				negated: true
				post: UziBlockNode empty].	
		[#doUntilFalse:]
		-> [UziLoopNode 
				pre: (rcvr concat: args first copyWithoutLastStatement)
				condition: args first lastStatement
				negated: false
				post: UziBlockNode empty].
					
		"Scripting"
		[#start] 
		-> [UziScriptStartNode script: rcvr].
		[#stop] 
		-> [UziScriptStopNode script: rcvr].
		[#value]
		-> [UziScriptCallNode script: rcvr]
	} 
	otherwise: [nil]