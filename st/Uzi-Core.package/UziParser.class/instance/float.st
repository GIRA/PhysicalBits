grammar-literals
float
	^ 'Infinity' asParser / '-Infinity' asParser / 'NaN' asParser / (integer, $. asParser, digits) flatten  
		map: [:val | Float readFrom: val]