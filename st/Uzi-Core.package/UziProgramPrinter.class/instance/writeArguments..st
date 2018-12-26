encoding scripts
writeArguments: args
	args
		select: [:var | var name notNil]
		thenDo: [:var |  
			self indent;	
				writeLine: 'registerArgument: {1}'
				format: { var name asSymbol storeString };
				indent;
				writeLine: 'value: {1};'
				format: { (var value ifNil: [0]) storeString }]