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
	
		"Scripting"
		[#start] -> [self halt].
		[#stop] -> [self halt]
	} otherwise: [nil].