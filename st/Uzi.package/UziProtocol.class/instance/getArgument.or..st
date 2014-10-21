private
getArgument: byte or: errorBlock
	(self isArgument: byte)
		ifTrue: [^ self getArgument: byte]
		ifFalse: [Transcript 
				show: 'Uzi - Invalid argument: ';
				show: byte;
				cr.
			^ errorBlock value]