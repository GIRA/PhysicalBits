specifying
group: aOneArgumentBlock
	"Defines a priority group by evaluating aOneArgumentBlock."
	
	operators := Dictionary new.
	parser := [ 
		aOneArgumentBlock value: self.
	 	self buildOn: parser ]
			ensure: [ operators := nil ]