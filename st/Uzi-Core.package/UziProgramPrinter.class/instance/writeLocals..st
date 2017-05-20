encoding scripts
writeLocals: locals 
	locals
		select: [:var | var name notNil]
		thenDo: [:var |  
			self indent;
				writeLine: 'registerLocal: #{1} value: {2};'
				format: { var name . (var value ifNil: [0]) storeString }]