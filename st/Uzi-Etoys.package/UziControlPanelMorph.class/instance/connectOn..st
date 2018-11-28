connection
connectOn: port
	protocol connectOn: port.	
	pinTexts keysDo: [:pin | self setPin: pin report: true]