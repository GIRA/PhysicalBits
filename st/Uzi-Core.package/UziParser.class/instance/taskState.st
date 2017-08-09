grammar-scripts
taskState
	^ ws , ('running' asParser / 'stopped' asParser / 'once' asParser) flatten , ws
	map: [:ign1 :str :ign | str asSymbol]