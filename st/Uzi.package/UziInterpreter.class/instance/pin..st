private
pin: pinNumber
	^ pinNumber < 14
		ifTrue: [arduino digitalPin: pinNumber]
		ifFalse: [arduino analogPin: pinNumber - 14].
