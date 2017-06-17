grammar-scripts
scriptState
	^ ws , ('ticking' asParser / 'stopped' asParser / 'once' asParser) flatten , ws
	map: [:ign1 :str :ign | str asSymbol]