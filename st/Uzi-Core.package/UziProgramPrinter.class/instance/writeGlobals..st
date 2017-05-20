encoding programs
writeGlobals: globals
	globals 
		select: [:var | var name notNil]
		thenDo: [:var |  
			self indent;
				writeLine: 'registerGlobal: #{1} value: {2};'
				format: { var name . (var value ifNil: [0]) storeString }]