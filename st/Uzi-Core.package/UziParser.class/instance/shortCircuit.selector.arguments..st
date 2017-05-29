building messages
shortCircuit: rcvr selector: selector arguments: args	
	^  selector caseOf: {
		[#'&&'] -> [UziShortCircuitAndNode left: rcvr right: args first].
		[#'||'] -> [UziShortCircuitOrNode left: rcvr right: args first].
	} 
	otherwise: [nil]