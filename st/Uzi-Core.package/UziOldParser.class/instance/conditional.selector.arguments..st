building messages
conditional: rcvr selector: selector arguments: args	
	^  selector caseOf: {
		"Conditionals"
		[#ifTrue:] 
		-> [UziConditionalNode condition: rcvr true: args first].
		[#ifFalse:] 
		-> [UziConditionalNode condition: rcvr false: args first].
		[#ifTrue:ifFalse:] 
		-> [UziConditionalNode condition: rcvr true: args first false: args second].
		[#ifFalse:ifTrue:] 
		-> [UziConditionalNode condition: rcvr true: args second false: args first]
	} 
	otherwise: [nil]