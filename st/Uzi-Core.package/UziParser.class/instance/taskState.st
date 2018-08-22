grammar-scripts
taskState
	^ ws , ('running' asParser / 'stopped' asParser) flatten optional, ws
	map: [:ign1 :str :ign | str ifNotNil: [str asSymbol]]