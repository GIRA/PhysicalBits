building messages
specialMessageSend: rcvr selector: selector arguments: args
	"
	If the selector is a special message then build the corresponding node
	and return it. Otherwise return nil.
	"
	^ selector caseOf: {
		"Conditionals"
		[#ifTrue:] 
		-> [UziConditionalNode condition: rcvr true: args first].
		[#ifFalse:] 
		-> [UziConditionalNode condition: rcvr false: args first].
		[#ifTrue:ifFalse:] 
		-> [UziConditionalNode condition: rcvr true: args first false: args second].
		[#ifFalse:ifTrue:] 
		-> [UziConditionalNode condition: rcvr true: args second false: args first].
		
		"Loops"
		[#whileTrue:]
		-> [UziLoopNode condition: rcvr body: args first].
		[#whileFalse:]
		-> [UziLoopNode condition: rcvr body: args first negated: true].
		[#whileTrue]
		-> [UziLoopNode condition: rcvr].
		[#whileFalse]
		-> [UziLoopNode condition: rcvr negated: true].
		[#doWhileTrue:]
		-> [UziLoopNode condition: args first body: rcvr evaluateBodyFirst: true].	
		[#doWhileFalse:]
		-> [UziLoopNode condition: args first body: rcvr negated: true evaluateBodyFirst: true].		
		[#untilTrue:]
		-> [UziLoopNode condition: rcvr body: args first negated: true].
		[#untilFalse:]
		-> [UziLoopNode condition: rcvr body: args first].
		[#untilTrue]
		-> [UziLoopNode condition: rcvr negated: true].
		[#untilFalse]
		-> [UziLoopNode condition: rcvr].
		[#doUntilTrue:]
		-> [UziLoopNode condition: args first body: rcvr negated: true evaluateBodyFirst: true].	
		[#doUntilFalse:]
		-> [UziLoopNode condition: args first body: rcvr evaluateBodyFirst: true].
		
		"Scripting"
		[#start] -> [self halt].
		[#stop] -> [self halt]
	} otherwise: [nil].