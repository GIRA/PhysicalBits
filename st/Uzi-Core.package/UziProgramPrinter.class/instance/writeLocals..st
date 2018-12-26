encoding scripts
writeLocals: locals 
	locals
		select: [:var | var name notNil]
		thenDo: [:var |  
			self indent;
				writeLine: 'registerLocal: {1}'
				format: { var name asSymbol storeString };
				indent;
				writeLine: 'value: {1};'
				format: { (var value ifNil: [0]) storeString }]