encoding scripts
writeArguments: args
	args
		select: [:var | var name notNil]
		thenDo: [:var |  
			self indent;
				writeLine: 'registerArgument: #{1} value: {2};'
				format: { var name . (var value ifNil: [0]) storeString }]