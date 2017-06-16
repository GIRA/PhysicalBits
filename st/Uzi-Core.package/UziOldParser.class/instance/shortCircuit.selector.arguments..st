building messages
shortCircuit: rcvr selector: selector arguments: args	
	^  selector caseOf: {
		[#'&&'] -> [UziLogicalAndNode left: rcvr right: args first].
		[#'||'] -> [UziLogicalOrNode left: rcvr right: args first].
	} 
	otherwise: [nil]