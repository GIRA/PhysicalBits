accessing-pins
getPin: pinNumber
	^ self pins 
		at: pinNumber 
		ifAbsentPut: [
			self setPin: pinNumber report: true.
			UziPin number: pinNumber]