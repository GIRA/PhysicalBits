grammar-imports
importsList
	^ ws, import star, ws
		map: [:ign1 :list :ign2 |
			"If any of the imports is nil it means the file was not found, return an appropriate failure"
			list detect: [:each | each value isNil]
				ifFound: [:each | PPFailure message: 'File not found' at: each key start]
				ifNone: ["Then look for a parse error. If found return it specifying the correct position"
					list detect: [:each | each value isPetitFailure] 
						ifFound: [:each | PPFailure 
							message: each value message
							at: each key parsedValue, ':', each value position asString]
						ifNone: [list collect: [:each | each value]]]]